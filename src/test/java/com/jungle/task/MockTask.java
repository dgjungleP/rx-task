package com.jungle.task;


import lombok.Data;

@Data
public class MockTask implements Task {

    private Integer id;

    public MockTask(Integer id) {
        this.id = id;
    }
}
