package com.dmm.task.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class PostController {

	@Autowired
	private TasksRepository repo;

	/**
	 * 投稿の一覧表示.
	 * 
	 * @param model モデル
	 * @return 遷移先
	 */
	@GetMapping("/posts")
	public String posts(Model model) {
		// 逆順で投稿をすべて取得する
		List<Tasks> list = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
//    Collections.reverse(list); //普通に取得してこちらの処理でもOK
		model.addAttribute("posts", list);
		TaskForm postForm = new TaskForm();
		model.addAttribute("postForm", postForm);
		return "/posts";
	}

	/**
	 * 投稿を作成.
	 * 
	 * @param postForm 送信データ
	 * @param user     ユーザー情報
	 * @return 遷移先
	 */
	@PostMapping("/posts/create")
	public String create(@Validated TaskForm postForm, BindingResult bindingResult,
			@AuthenticationPrincipal AccountUserDetails user, Model model) {
		// バリデーションの結果、エラーがあるかどうかチェック
		if (bindingResult.hasErrors()) {
			// エラーがある場合は投稿登録画面を返す
			List<Tasks> list = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
			model.addAttribute("posts", list);
			model.addAttribute("postForm", postForm);
			return "/posts";
		}

		Tasks post = new Tasks();
		post.setName(user.getName());
		post.setTitle(postForm.getTitle());
		post.setText(postForm.getText());
		post.setDate(LocalDateTime.now());

		repo.save(post);

		return "redirect:/posts";
	}

	/**
	 * 投稿を削除する
	 * 
	 * @param id 投稿ID
	 * @return 遷移先
	 */
	@PostMapping("/posts/delete/{id}")
	public String delete(@PathVariable Integer id) {
		repo.deleteById(id);
		return "redirect:/posts";
	}
}