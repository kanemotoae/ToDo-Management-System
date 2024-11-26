
package com.dmm.task.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;

@Controller
public class TaskController {

    @Autowired
    private TasksRepository repo;
    
    @GetMapping("/main")
    public String tasks(Model model) {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        String yearMonth = String.format("%d年%02d月", year, month);
        model.addAttribute("month", yearMonth);  // 'month' をHTMLに渡す
     
        // カレンダーを格納する2次元リスト
        List<List<LocalDate>> monthList = new ArrayList<>();
        
        // その月の1日を取得
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        // 月の最終日を取得
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        
        // カレンダーの開始日（前月の最後の週の月曜日）
        LocalDate startDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() % 7);
        // カレンダーの終了日（翌月の最初の週の土曜日）
        LocalDate endDate = lastDayOfMonth.plusDays(6 - lastDayOfMonth.getDayOfWeek().getValue());
        
        // カレンダーの各日付を週ごとに分けて格納
        List<LocalDate> week = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            week.add(currentDate);
            if (week.size() == 7) {
                monthList.add(new ArrayList<>(week)); // 1週間分を追加
                week.clear(); // リセット
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // タスクの取得
        List<Tasks> tasks = repo.findAll(); // すべてのタスクを取得
        MultiValueMap<LocalDate, Tasks> taskMap = new LinkedMultiValueMap<>();
        
        // 日付に紐づけてタスクをマッピング
        for (Tasks task : tasks) {
            taskMap.add(task.getDate().toLocalDate(), task);
        }
        
        // モデルにデータを追加
        LocalDate prev = firstDayOfMonth.minusMonths(1);
        LocalDate next = lastDayOfMonth.plusMonths(1);
        model.addAttribute("prev", prev);  // 前月をHTMLに渡す
        model.addAttribute("next", next);  // 次月をHTMLに渡す
        model.addAttribute("matrix", monthList); // カレンダー（2次元リスト）
        model.addAttribute("tasks", taskMap);    // タスク
        
        return "/main";
    }
}