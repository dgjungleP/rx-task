package com.jungle.task;

import java.util.List;
import java.util.Optional;

public interface TaskQueue {

    void push(Task task);

    Optional<Task> take();

    List<Optional<Task>> take(int num);
}
