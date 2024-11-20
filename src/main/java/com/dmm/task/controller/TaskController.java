package com.dmm.task.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TaskController {

    @GetMapping("/main")
    public String main() {
        // 1. 2次元表になるので、ListのListを用意する
        List<List<LocalDate>> calendar = new ArrayList<>();

        // 2. 1週間分のLocalDateを格納するListを用意する
        List<LocalDate> week = new ArrayList<>();

        // 3. その月の1日のLocalDateを取得する
        LocalDate firstDayOfMonth = LocalDate.of(2024, 11, 1);

        // 4. 曜日を表すDayOfWeekを取得し、曜日の値をマイナスして前月分のLocalDateを求める
        DayOfWeek dayOfWeek = firstDayOfMonth.getDayOfWeek();
        LocalDate startDate = firstDayOfMonth.minusDays(dayOfWeek.getValue());

        // 5. 1日ずつ増やしてLocalDateを求め、1週間分をListに格納する
        LocalDate currentDate = startDate;
        while (currentDate.isBefore(firstDayOfMonth.plusMonths(1).withDayOfMonth(1))) {
            week.add(currentDate);
            currentDate = currentDate.plusDays(1);

            // 1週間分詰めたらListをcalendarに追加して新しい週を作成
            if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                calendar.add(week);
                week = new ArrayList<>();
            }
        }

        // 6. 最終週の翌月分を計算し、最後のListに格納
        while (!week.isEmpty() && week.size() < 7) {
            week.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        if (!week.isEmpty()) {
            calendar.add(week);
 
        }model.addAttribute("calendar", calendar);



        return "main";
    }
}
