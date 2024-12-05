package com.dmm.task.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Autowired //依存性の注入
    private TasksRepository repo; //repo(変数名)にはDB操作を行うためのインスタンス(オブジェクト)が格納⇒DB操作を簡潔化
    
    
    
//★------------------------------------------------------------------------------------------------------------------★
    
    
    
    /**
     * カレンダー表示機能（月表示の左右に前月、翌月へのリンクを表示）
     * 
     * 
     *
     *
     */
    
    
    
    @GetMapping("/main") /*ブラウザが/mainを呼んだ時実行される処理
	HTMLのGet(データの取得)リクエストを特定のメソッドにマッピングする**/
public String tasks(@RequestParam(required = false, defaultValue = "") String date,
@AuthenticationPrincipal AccountUserDetails user, //追加
Model model) {

LocalDate firstDayOfMonth;

/* 引数はdateとmodelオブジェクト */

if (date.isEmpty()) {
firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
} else {
firstDayOfMonth = LocalDate.parse(date).withDayOfMonth(1);
} //つまり、/mainで呼び出されたときなどは、1日から始まる当月カレンダーを返す

// 月の最終日を取得
LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
LocalDateTime fromDateTime = firstDayOfMonth.atStartOfDay(); // 開始日の 00:00:00
LocalDateTime toDateTime = lastDayOfMonth.atTime(23, 59, 59); // 終了日の 23:59:59

List<Tasks> tasksList;
if (user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
    // 管理者の場合、すべてのタスクを取得
    tasksList = repo.findAllByDateBetween(fromDateTime, toDateTime);
} else {
    // 一般ユーザーの場合、自分のタスクのみ取得
    tasksList = repo.findByDateBetween(fromDateTime, toDateTime, user.getUsername());
}


MultiValueMap<LocalDate, Tasks> taskMap = new LinkedMultiValueMap<>();
for (Tasks task : tasksList) {
    taskMap.add(task.getDate(), task);
}


int year = firstDayOfMonth.getYear();
int month = firstDayOfMonth.getMonthValue();
String yearMonth = String.format("%d年%02d月", year, month);
model.addAttribute("month", yearMonth);

// カレンダーを格納する2次元リスト
List<List<LocalDate>> monthList = new ArrayList<>();

// カレンダーの終了日（翌月の最初の週の土曜日）
LocalDate endDate = lastDayOfMonth.plusDays(7 - lastDayOfMonth.getDayOfWeek().getValue() % 7);
// カレンダーの開始日（前月の最後の週の月曜日）
LocalDate startDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() % 7);

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

// 月表示の前後リンク
LocalDate prev = firstDayOfMonth.minusMonths(1);
LocalDate next = firstDayOfMonth.plusMonths(1);

model.addAttribute("prev", prev);
model.addAttribute("next", next);
model.addAttribute("matrix", monthList);
model.addAttribute("tasks", taskMap);

return "/main";
}

    
//★------------------------------------------------------------------------------------------------------------------★
    
    
    
    /**
     * タスク登録画面表示機能（日付をクリックするとタスクの登録画面に遷移）
     * タスク登録機能
     * 
     * 
     * 
     */
    
    
    
    /*タスク登録画面に遷移する際には、選択した日付が必要なので {date}が必要
    一方で、フォーム送信時はデータが taskForm に含まれているため、{date} は不要**/
    
    //GETは 画面表示、POSTはDB保存
    
    
    
    @GetMapping("/main/create/{date}")
    public String create(Model model, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    	TaskForm taskForm = new TaskForm();
        taskForm.setDate(date); // そのまま LocalDate をセット
        model.addAttribute("taskForm", taskForm);
        return "create";
    }
    // @PathVariableはURLのパス部分からデータを取得するアノテーション


    @PostMapping("/main/create")
    public String createTask(@Validated TaskForm taskForm,
    						@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
    						BindingResult bindingResult, 
                            @AuthenticationPrincipal AccountUserDetails user, Model model) {

    	
        // バリデーションエラー(TaskFormで定義)があるかどうかチェック
    	
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskForm", taskForm);
            return "redirect:/create";  // エラーがあれば登録画面に戻す
        }

        // タスクを作成して、必要な情報を設定
        Tasks task = new Tasks();
        task.setName(user.getName());  // ログインユーザー名をセット
        task.setTitle(taskForm.getTitle());  // タイトルをフォームから設定
        task.setText(taskForm.getText());  // テキストをフォームから設定
        task.setDate(taskForm.getDate());  // フォームから受け取った日付を設定

        // タスクを保存
        repo.save(task);

        return "redirect:/main";  // 保存後、カレンダー画面へリダイレクト
    }








//★------------------------------------------------------------------------------------------------------------------★

    
    
    /**
     * タスク編集・削除機能
     * 
     * 
     * 
     * 
     */
    
    
    
    @GetMapping("/main/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
 
        Tasks task = repo.findById(id).get();  // get()で必ず値を取得

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
       

        Tasks task = repo.findById(id).get();  // タスクを取得
        task.setTitle(taskForm.getTitle());    // タイトルを更新
        task.setText(taskForm.getText());      // テキストを更新
        task.setDate(taskForm.getDate());      // 日付を更新
        task.setDone(taskForm.isDone());       // 完了フラグを更新

        repo.save(task);  // タスクを保存

        return "redirect:/main";  // 保存後、カレンダー画面へリダイレクト
    }
    
    
    
    @PostMapping("/main/delete/{id}")
    public String deleteTask(@PathVariable Integer id) {
        repo.deleteById(id);  // タスクを削除
        return "redirect:/main";  // 削除後、カレンダー画面へリダイレクト
    }
}

