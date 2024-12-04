package com.dmm.task.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class TaskController {

    @Autowired
    private TasksRepository repo;

    // カレンダー表示機能
    @GetMapping("/main")
    public String tasks(@RequestParam(required = false, defaultValue = "") String date, 
                        @AuthenticationPrincipal AccountUserDetails user, 
                        Model model) {
        LocalDate firstDayOfMonth;
        if (date.isEmpty()) {
            firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        } else {
            firstDayOfMonth = LocalDate.parse(date).withDayOfMonth(1);
        }

        List<Tasks> list;
        // adminの場合は全員のタスクを表示
        if (user.getRole().equals("admin")) {
            list = repo.findAll();
        } else {
            // userの場合は自分のタスクのみ表示
            list = repo.findByUserId(user.getId());
        }

        int year = firstDayOfMonth.getYear();
        int month = firstDayOfMonth.getMonthValue();
        String yearMonth = String.format("%d年%02d月", year, month);
        model.addAttribute("month", yearMonth);

        // カレンダーを格納する2次元リスト
        List<List<LocalDate>> monthList = new ArrayList<>();

        // 月の最終日を取得
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

        // カレンダーの終了日
        LocalDate endDate = lastDayOfMonth.plusDays(7 - lastDayOfMonth.getDayOfWeek().getValue() % 7);
        // カレンダーの開始日
        LocalDate startDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() % 7);

        // カレンダーの日付を週ごとに分けて格納
        List<LocalDate> week = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            week.add(currentDate);
            if (week.size() == 7) {
                monthList.add(new ArrayList<>(week));
                week.clear();
            }
            currentDate = currentDate.plusDays(1);
        }

        // タスクの取得とマッピング
        MultiValueMap<LocalDate, Tasks> taskMap = new LinkedMultiValueMap<>();
        for (Tasks task : list) {
            taskMap.add(task.getDate(), task);
        }

        // 月表示の前後リンク
        LocalDate prev = firstDayOfMonth.minusMonths(1); 
        LocalDate next = firstDayOfMonth.plusMonths(1); 

        model.addAttribute("prev", prev);
        model.addAttribute("next", next);
        model.addAttribute("matrix", monthList);
        model.addAttribute("tasks", taskMap);

        return "/main";
    }

    // タスク登録画面表示機能
    @GetMapping("/main/create/{date}")
    public String create(Model model, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        TaskForm taskForm = new TaskForm();
        taskForm.setDate(date); 
        model.addAttribute("taskForm", taskForm);
        return "create";
    }

    // タスク登録機能
    @PostMapping("/main/create")
    public String createTask(@Validated TaskForm taskForm,
                             @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                             BindingResult bindingResult, 
                             @AuthenticationPrincipal AccountUserDetails user, 
                             Model model) {

        // バリデーションエラーがあるかどうかチェック
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskForm", taskForm);
            return "create"; 
        }

        // タスクを作成して、必要な情報を設定
        Tasks task = new Tasks();
        task.setName(user.getName());
        task.setTitle(taskForm.getTitle());
        task.setText(taskForm.getText());
        task.setDate(taskForm.getDate()); 
        task.setUserId(user.getId()); // ユーザーIDをセット

        // タスクを保存
        repo.save(task);

        return "redirect:/main"; 
    }

    // タスク編集・削除機能
    @GetMapping("/main/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Tasks task = repo.findById(id).orElse(null); 

        // TaskFormをセット
        TaskForm taskForm = new TaskForm();
        taskForm.setTitle(task.getTitle());
        taskForm.setText(task.getText());
        taskForm.setDate(task.getDate());

        model.addAttribute("taskForm", taskForm);
        model.addAttribute("task", task);

        return "/edit";
    }

    @PostMapping("/main/edit/{id}")
    public String editTask(@PathVariable Integer id, @Validated TaskForm taskForm, Model model) {
        Tasks task = repo.findById(id).orElse(null); 
        task.setTitle(taskForm.getTitle());
        task.setText(taskForm.getText());
        task.setDate(taskForm.getDate());
        task.setDone(taskForm.isDone());

        repo.save(task);
        return "redirect:/main"; 
    }

    @PostMapping("/main/delete/{id}")
    public String deleteTask(@PathVariable Integer id) {
        repo.deleteById(id); 
        return "redirect:/main"; 
    }
}
