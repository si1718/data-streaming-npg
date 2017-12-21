package data.streaming.test;

import data.streaming.scheduled.RecommenderTask;


public class TestRecommender {

    public static void main(String[] args) {

        RecommenderTask recommenderTask = new RecommenderTask();

        recommenderTask.prepare();
    }
}
