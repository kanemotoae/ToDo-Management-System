package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    
 
    /* ブラウザが/mainを呼んだ時実行される処理
    HTMLのGet(データの取得)リクエストを特定のメソッドにマッピングする */
    
    @GetMapping("/main")
    public String main(Model model, @AuthenticationPrincipal AccountUserDetails user,
    								 @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        // 週と日を格納する二次元のListを用意する
        List<List<LocalDate>> month = new ArrayList<>();

        // 1週間分のLocalDateを格納するListを用意する
        List<LocalDate> week = new ArrayList<>();

        // 日にちを格納する変数を用意する
        LocalDate day, start, end;

        // 今月 or 前月 or 翌月を判定
        if(date == null) {
          // その月の1日を取得する
          day = LocalDate.now();  // 現在日時を取得
          day = LocalDate.of(day.getYear(), day.getMonthValue(), 1);  // 現在日時からその月の1日を取得
        }else {
          day = date;  // 引数で受け取った日付をそのまま使う
        }

        // カレンダーの ToDo直下に「yyyy年mm月」と表示
        model.addAttribute("month", day.format(DateTimeFormatter.ofPattern("yyyy年MM月")));

        // 前月のリンク
        model.addAttribute("prev", day.minusMonths(1));

        // 翌月のリンク
        model.addAttribute("next", day.plusMonths(1));

        // 前月分の LocalDateを求める
    	DayOfWeek w = day.getDayOfWeek();  // 当該日の曜日を取得
    	if (w != DayOfWeek.SUNDAY) {  // 1日が日曜以外であれば
    	  day = day.minusDays(w.getValue());  // 1日からマイナス
    	}
    	start = day;

        // 1週目（1日ずつ増やして 週のリストに格納していく）
        for(int i = 1; i <= 7; i++) {
          week.add(day);  // 週のリストへ格納
          day = day.plusDays(1);  // 1日進める
        }    
        month.add(week);  // 1週目のリストを、月のリストへ格納する

        week = new ArrayList<>();  // 次週のリストを新しくつくる

        // 2週目
    	int currentMonth = day.getMonthValue();
    	int leftOfMonth = day.lengthOfMonth() - day.getDayOfMonth();
    	leftOfMonth = day.lengthOfMonth() - leftOfMonth;
    	leftOfMonth = 7 - leftOfMonth;

    	for (int i = 7; i <= day.lengthOfMonth() + leftOfMonth; i++) {
    	  week.add(day);  // 週のリストへ格納

    	  w = day.getDayOfWeek();
    	  if(w == DayOfWeek.SATURDAY) {  // 土曜日だったら
    	    month.add(week);  // 当該週のリストを、月のリストへ格納する
    	    week = new ArrayList<>();  // 次週のリストを新しくつくる
    	  }

    	  day = day.plusDays(1);  // 1日進める

    	  if (currentMonth != day.getMonthValue()) {
    		  // 翌月になったら抜ける
    		  break;
    	  }
    	}

    	// 最終週の翌月分
    	w = day.getDayOfWeek();
    	if(w != DayOfWeek.SUNDAY) {
    		DayOfWeek endofmonth = day.getDayOfWeek();
    		int next = 7 - endofmonth.getValue();
    		if (next == 0) {
    			next = 7;
    		}
    		for (int n = 1; n <= next; n++) {
    			week.add(day);
    			day = day.plusDays(1);
    		}
    		month.add(week);
    	}

    	end = day;

        // 日付とタスクを紐付けるコレクション
        MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();

        // リポジトリからタスクを取得
        List<Tasks> list;
        if(user.getUsername().equals("admin")) {
          // 管理者だったら
          list = repo.findAllByDateBetween(start.atTime(0,0), end.atTime(0,0));
        } else {
          // ユーザーだったら
          list = repo.findByDateBetween(start.atTime(0, 0),end.atTime(0, 0), user.getName());
        }

        // 取得したタスクをコレクションに追加
        for(Tasks task : list) {
          tasks.add(task.getDate().toLocalDate(), task);
        }

        // カレンダーのデータをHTMLに連携
        model.addAttribute("matrix", month);

        // コレクションのデータをHTMLに連携
        model.addAttribute("tasks", tasks);

        // HTMLを表示
        return "main";
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
    
    //変更しない！！！！！
    @GetMapping("/main/create/{date}")
    public String create(Model model, 
    					@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) { 
        return "create";
    }
    
    

    // @PathVariableはURLのパス部分からデータを取得するアノテーション

    @PostMapping("/main/create")
    public String createTask(@Validated TaskForm taskForm, 
                             @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                             BindingResult bindingResult, 
                             @AuthenticationPrincipal AccountUserDetails user, Model model) {


        //バリデーションエラー(TaskFormで定義)があるかどうかチェック
        if (bindingResult.hasErrors()) {
            model.addAttribute("taskForm", taskForm);
            return "create";
        }

        // タスクを作成して、必要な情報を設定
        Tasks task = new Tasks();
        task.setName(user.getName());  // ログインユーザー名をセット
        task.setTitle(taskForm.getTitle());  // タイトルをフォームから設定
        task.setText(taskForm.getText());  // テキストをフォームから設定
        task.setDate(taskForm.getDate().atStartOfDay()); 

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
    public String edit(@PathVariable Integer id,Model model) {
 
        Tasks task = repo.findById(id).get();  // get()で必ず値を取得

        // TaskFormをセット
        TaskForm taskForm = new TaskForm();
        taskForm.setTitle(task.getTitle());
        taskForm.setText(task.getText());
        //taskForm.setDate(task.getDate().);

        model.addAttribute("taskForm", taskForm);
        model.addAttribute("task", task);

        return "/edit";
    }



    
    
    @PostMapping("/main/edit/{id}")
    public String editTask(@PathVariable Integer id, @Validated TaskForm taskForm,
    						@AuthenticationPrincipal AccountUserDetails user,Model model) {
    							

        Tasks task = repo.findById(id).get();  // タスクを取得
        
        task.setTitle(taskForm.getTitle());    // タイトルを更新
        task.setText(taskForm.getText());      // テキストを更新
        task.setDate(taskForm.getDate().atStartOfDay()); // 日付を更新
        
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