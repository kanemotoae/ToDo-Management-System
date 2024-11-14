package com.dmm.task.form;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class PostForm {
	// titleへのバリデーション設定を追加
	@Size(min = 1, max = 200)
	private String title;
	// textへのバリデーション設定を追加
	@Size(min = 1, max = 200)
	private String text;
}