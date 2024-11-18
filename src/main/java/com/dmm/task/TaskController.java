package com.dmm.task;

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
        // 現在の日付を取得し、その月の1日を基準日とする
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);

        // その月のカレンダーを格納するためのリスト（2次元リスト）
        List<List<LocalDate>> month = new ArrayList<>();

        // 最初の週の計算
        List<LocalDate> week = new ArrayList<>();
        DayOfWeek startDayOfWeek = firstDayOfMonth.getDayOfWeek();
        LocalDate startDate = firstDayOfMonth.minusDays(startDayOfWeek.getValue() - 1);

        // 1ヶ月分の日数を取得
        int daysInMonth = firstDayOfMonth.lengthOfMonth();

        // 月初から月末までの日付を処理（前月・翌月分も含む）
        LocalDate currentDate = startDate;
        int totalDays = daysInMonth + startDayOfWeek.getValue() - 1; // カレンダーの表示範囲を計算
        for (int i = 0; i < totalDays + 7; i++) { // 翌月分も含めて表示範囲を拡張
            week.add(currentDate);
            if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                month.add(week);
                week = new ArrayList<>();
            }
            currentDate = currentDate.plusDays(1);
        }
        // 最後の週を追加
        if (!week.isEmpty()) {
            month.add(week);
        }

        // 管理者向けのタスク表示（未実装）
        // 必要に応じて、全員分のタスクを取り出してカレンダーと関連付ける

        return "main";
    }
}
