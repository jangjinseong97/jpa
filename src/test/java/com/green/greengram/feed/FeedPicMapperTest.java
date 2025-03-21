package com.green.greengram.feed;

import com.green.greengram.TestUtils;
import com.green.greengram.feed.model.FeedPicDto;
import com.green.greengram.feed.model.FeedPicVo;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FeedPicMapperTest {
    @Autowired
        FeedPicMapper feedPicMapper;
    @Autowired
        FeedPicTestMapper feedPicTestMapper;

    @Test
        void insFeedPicNoFeedIdThrowsForeignKeyException() {
            FeedPicDto givenParam = new FeedPicDto();
            givenParam.setFeedId(10L);
            givenParam.setPics(new ArrayList<>(1));
            givenParam.getPics().add("a.jpg");
            assertThrows(DataIntegrityViolationException.class, () -> feedPicMapper.insFeedPic(givenParam));
    }

    @Test
    void insFeedPicNoPicThrowNotNullException() {
        FeedPicDto givenParam = new FeedPicDto();
        givenParam.setFeedId(1L);
        givenParam.setPics(new ArrayList<>());
        assertThrows(BadSqlGrammarException.class, () -> feedPicMapper.insFeedPic(givenParam));
    }

    @Test
    void insFeedPic(){

        String[] pics = {"a.jpg", "b.jpg", "c.jpg"};
        FeedPicDto givenParam = new FeedPicDto();
        givenParam.setFeedId(5L);
        givenParam.setPics(new ArrayList<>(pics.length));
        for(int i=0; i<pics.length; i++){
            givenParam.getPics().add(pics[i]);
        }
//        givenParam.getPics().add("a.jpg"); 한번에 작성
//        givenParam.getPics().add("b.jpg");

        List<FeedPicVo> feedPicListBefore = feedPicTestMapper.selFeedPicListByFeedId(givenParam.getFeedId());
        int actualAffectedRows = feedPicMapper.insFeedPic(givenParam);
        List<FeedPicVo> feedPicListAfter = feedPicTestMapper.selFeedPicListByFeedId(givenParam.getFeedId());


        // 아래 containsAll 이 돌아가는 방식
        // feedPicListAfter 에서 pic만 뽑아내서 List<String>으로 변현하고 채크 혹은 스트림을 이용
        List<String> feedOnlyPicList = new ArrayList<>(feedPicListAfter.size());
//        for(int i=0; i<feedPicListAfter.size(); i++){
//            feedOnlyPicList.add(feedPicListAfter.get(i).getPic());
//        }
        for(FeedPicVo pic : feedPicListAfter){
            feedOnlyPicList.add(pic.getPic());
        }

        List<String> picsList = Arrays.asList(pics);
        for(int i=0; i<picsList.size(); i++){
            String pic = picsList.get(i);
            System.out.printf("%s - contains : %b\n", pic, feedPicListAfter.contains(pic));
//            if(!feedPicListAfter.contains(pic)){
//                return false;
//            }
        }
        // 스트림 이용 (predicate 리턴타입o boolean , 파라미터o FeedPicVo)
        feedPicListAfter.stream().allMatch(feedPicVo -> picsList.contains(feedPicVo.getPic()));

        assertAll(
                // forEach 에서 내부적으로 stream() 으로 만들어서 사용
                () -> feedPicListAfter.forEach(feedPicVo -> TestUtils.assertCurrenTimeStamp(feedPicVo.getCreatedAt()))
                // ,() ->  for(FeedPicVo feedPicVo : feedPicListAfter) {
                //              TestUtils.assertCurrentTimestamp(feedPicVo.getCreatedAt()); }
                // 위의 forEach 와 같음

                , () -> assertEquals(pics.length, actualAffectedRows)
                , () -> assertEquals(0, feedPicListBefore.size())
                , () -> assertEquals(pics.length, feedPicListAfter.size())
                , () -> assertTrue(feedOnlyPicList.containsAll(Arrays.asList(pics)))
                , () -> assertTrue(feedPicListAfter.stream().allMatch(feedPicVo -> picsList.contains(feedPicVo.getPic())))

                , () -> assertTrue(feedPicListAfter.stream() // 스트림 생성 Stream<FeedPicVo>
                                                            .map(FeedPicVo :: getPic) // 같은 크기의 새로은 반환 Stream<String> ["a.jpg" , "b.jpg" , "c.jpg"]
                                                            .filter(pic -> picsList.contains(pic)) // 필터는 연산 결과가 true인 것만 뽑아내서 새로운 스트림 반환 Stream<String> ["a.jpg" , "b.jpg" , "c.jpg"]
                                                            .limit(picsList.size()) // 스트림 크기 제한, 이전 스트림의 크기가 10개인데 limit(2)를 하면 2개짜리 스트림이 반환
                                                            .count() == picsList.size())

                // function return type 0 (String) , parameter 0 (FeedPicVo)
                , () -> assertTrue(feedPicListAfter.stream().map(feedPicVo -> feedPicVo.getPic()) // ["a.jpg" , "b.jpg" , "c.jpg"]
                        .toList() // 스트림 > List
                        .containsAll(Arrays.asList(pics)))


        );

    }
    @Test
    void insFeedPic1(){
        String [] pics = {"a.jpg", "b.jpg", "c.jpg"};
        FeedPicDto givenParam = new FeedPicDto();
        givenParam.setFeedId(1L);
        givenParam.setPics(new ArrayList<>(pics.length));
        for(String pic: pics){
            givenParam.getPics().add(pic);
        }
        int actualAffectedRows = feedPicMapper.insFeedPic(givenParam);
        assertEquals(pics.length, actualAffectedRows);
    }

    @Test
    void insFeedPic_PicStringLengthMoreThan50_ThrowException(){
        FeedPicDto givenParam = new FeedPicDto();
        givenParam.setFeedId(1L);
        givenParam.setPics(new ArrayList<>(1));
        givenParam.getPics().add("123456789_123456789_123456789_123456789_123456789_12");
        assertThrows(BadSqlGrammarException.class, () -> feedPicMapper.insFeedPic(givenParam));
    }
}