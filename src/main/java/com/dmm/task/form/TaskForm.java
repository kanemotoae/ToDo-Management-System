package com.dmm.task.form;

import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class TaskForm {
    // titleへのバリデーション設定を追加
    @Size(min = 1, max = 200)
    private String title;

    // textへのバリデーション設定を追加
    @Size(min = 1, max = 200)
    private String text;

    // 追加: タスクの日付を保持するフィールド
    private LocalDateTime date;
    
    /*編集画面で完了にチェックを入れると、
    カレンダー上にチェックマーク（✅）を表示する**/
    private boolean done;

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
