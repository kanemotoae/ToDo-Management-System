package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
//import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;

@Controller
public class TaskController {

    //@Autowired
    //private TasksRepository repo;

    @GetMapping("/tasks")
    public String tasks(Model model) {
        // ListのListを用意する: 2次元リストでカレンダーを格納
        List<List<LocalDate>> month = new ArrayList<>();
        MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();// 各日付に紐づけられるタスクのリスト
        model.addAttribute("tasks", tasks);
        model.addAttribute("calendar", month); // カレンダーをビューに渡す
        TaskForm taskForm = new TaskForm();
        model.addAttribute("taskForm", taskForm);
        return "main"; // カレンダーを表示するためのビュー名
    }

    @PostMapping("/tasks/create")
    private List<List<LocalDate>> generateCalendar(int year, int month) {
        List<List<LocalDate>> calendar = new ArrayList<>();

        // 対象月の初日と開始日（前月分を含む）の計算
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate startDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue()); // 前月の日付を補完

        LocalDate currentDate = startDate;
        while (currentDate.getMonthValue() == month || currentDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            List<LocalDate> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                week.add(currentDate);
                currentDate = currentDate.plusDays(1); // 1日ずつ増加
            }
            calendar.add(week);
        }

        // 2週目以降は1日ずつ日を増やして格納
        while (currentDate.getMonthValue() == month) {
            List<LocalDate> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                week.add(currentDate);
                currentDate = currentDate.plusDays(1); // 1日ずつ増加
                // 月末になった場合
                if (currentDate.getMonthValue() != month) {
                    break;
                }
            }
            calendar.add(week);
        }

        // 最終週を修正して翌月の日付を補完
        List<LocalDate> lastWeek = calendar.get(calendar.size() - 1);
        LocalDate nextMonthStartDate = firstDayOfMonth.plusMonths(1).withDayOfMonth(1);

        // 最終週に足りない日を追加（翌月の初めの日付）
        for (int i = lastWeek.size(); i < 7; i++) {
            lastWeek.add(nextMonthStartDate);
            nextMonthStartDate = nextMonthStartDate.plusDays(1);
        }

        // 1週間分が揃ったカレンダーリストを返す
        return calendar;
    }
}

