package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class TaskController {

    @Autowired
    private TasksRepository repo;
    
    @GetMapping("/main") // エンドポイントを /main に変更
    public String tasks(Model model) {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        
        // ListのListを用意する: 2次元リストでカレンダーを格納
        List<List<LocalDate>> monthList = new ArrayList<>();
        
        // 1週間分のLocalDateを格納するListを用意する
        List<LocalDate> week = new ArrayList<>();
        
        // その月の1日のLocalDateを取得する
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        
        // 曜日を表すDayOfWeekを取得し
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
        
        // 上で取得したLocalDateに曜日の値（DayOfWeek#getValue)をマイナスして前月分のLocalDateを求める
        LocalDate startDate = firstDayOfMonth.minusDays(dayOfWeekValue - 1);
        
        // 1日ずつ増やしてLocalDateを求めていき、2．で作成したListへ格納していく
        LocalDate currentDate = startDate;
        while (currentDate.getMonthValue() == firstDayOfMonth.getMonthValue() || currentDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            week.add(currentDate);
            currentDate = currentDate.plusDays(1);
        
            // 1週間分詰めたら1．のリストへ格納する
            if (week.size() == 7) {
                monthList.add(new ArrayList<>(week)); // 1週間分をmonthListに追加
                week.clear(); // 週ごとにリセット
            }
        }
        
        // 2週目以降は単純に1日ずつ日を増やしながらLocalDateを求めてListへ格納していき、
        while (currentDate.getMonthValue() == firstDayOfMonth.getMonthValue()) {
            week.add(currentDate);
            currentDate = currentDate.plusDays(1);
        
            // 土曜日になったら1．のリストへ格納して新しいListを生成する（月末を求めるにはLocalDate#lengthOfMonth()を使う）
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                monthList.add(new ArrayList<>(week));
                week.clear(); // 週ごとにリセット
            }
        }
        
        /* 最終週の翌月分をDayOfWeekの値を使って計算し、6．で生成したリストへ格納し、
        最後に1．で生成したリストへ格納する */
        while (week.size() < 7) {
            week.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        if (!week.isEmpty()) {
            monthList.add(new ArrayList<>(week));
        }
        
        // 管理者は全員分のタスクを見えるようにする
        List<Tasks> tasks = repo.findAll(); // すべてのタスクを取得
        model.addAttribute("tasks", tasks); // modelにタスクを追加
        
        // 各日付に紐づけられるタスクのリスト
        MultiValueMap<LocalDate, Tasks> taskMap = new LinkedMultiValueMap<>();
        model.addAttribute("taskMap", taskMap);
        model.addAttribute("calendar", month); // カレンダーをビューに渡す
        model.addAttribute("matrix", month);
        model.addAttribute("taskForm", tasks(model));
        return "/main";
    }

    /**
     * 投稿を作成.
     * 
     * @param postForm 送信データ
     * @param user     ユーザー情報
     * @return 遷移先
     */
    @PostMapping("/tasks/create")
    public String create(@Validated TaskForm taskForm, BindingResult bindingResult,
            @AuthenticationPrincipal AccountUserDetails user, Model model) {
        // バリデーションの結果、エラーがあるかどうかチェック
        if (bindingResult.hasErrors()) {
            // エラーがある場合は投稿登録画面を返す
            List<List<LocalDate>> month = new ArrayList<>();
            model.addAttribute("tasks", month);
            model.addAttribute("taskForm", taskForm);
            return "/main";
        }

        Tasks task = new Tasks();
        task.setName(user.getName());
        task.setTitle(taskForm.getTitle());
        task.setText(taskForm.getText());
        task.setDate(LocalDateTime.now());

        repo.save(task);

        return "redirect:/main"; // 遷移先を /main に変更
    }

    /**
     * 投稿を削除する
     * 
     * @param id 投稿ID
     * @return 遷移先
     */
    @PostMapping("/tasks/delete/{id}")
    public String delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/main"; // 遷移先を /main に変更
    }
}
