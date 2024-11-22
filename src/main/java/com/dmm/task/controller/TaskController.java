package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;

@Controller
public class TaskController {

    @GetMapping("/main")
    public String main(Model model) {
        // カレンダー生成処理
        List<List<LocalDate>> month = generateCalendar(2024, 11);
        MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();

        // "matrix" と "tasks" を連携
        model.addAttribute("tasks", tasks);

        return "main";
    }

    /**
     * 指定された年月のカレンダーを生成するメソッド
     *
     * @param year  年
     * @param month 月
     * @return 2次元リスト形式のカレンダー
     */
    private List<List<LocalDate>> generateCalendar(int year, int month) {
        List<List<LocalDate>> calendar = new ArrayList<>();

        // 対象月の初日と開始日（前月分を含む）の計算
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate startDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue());

        // カレンダーを生成
        LocalDate currentDate = startDate;
        while (currentDate.isBefore(firstDayOfMonth.plusMonths(1).withDayOfMonth(1)) || currentDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            List<LocalDate> week = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                week.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
            calendar.add(week);
        }

        return calendar;
    }
}
