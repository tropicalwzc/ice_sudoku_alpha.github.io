package com.example.icesudoku;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.Random;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.example.icesudoku.ice_sudoku_core;
public class MainActivity extends AppCompatActivity {
    //colors
    int originbackgroudcolor = Color.rgb(224, 224, 224);
    int focusbackgroudcolor = Color.rgb(248, 250, 222);

    int numbernormalcolor = Color.rgb(200, 200, 200);
    int numberfocuscolor = Color.rgb(44, 44, 44);

    private static final int MSG_SUCCESS = 0;
    RelativeLayout.LayoutParams[][] largebutton_params = new RelativeLayout.LayoutParams[9][9];
    boolean[][][] should_not_add_candidate_position = new boolean[9][9][10];
    TextView[][][] candidatebutton = new TextView[9][9][10];
    TextView analysis_board ;
    int[][][][] candidate_positions = new int[9][9][10][2];
    Button[][] sudobuttons = new Button[9][9];
    Button[] numbuttons = new Button[9];
    ImageButton[] penpencilbtns = new ImageButton[3];
    ImageButton[] delundobtns = new ImageButton[3];
    int[][][] sudo = new int[10][9][9];
    int[][] sudoext = new int[9][9];
    int[][][] player_candidate = new int[9][9][10];
    int[][] player_ans = new int[9][9];
    int stkid = 0;
    int[][][][] stk_player_candidate = new int[512][9][9][10];
    boolean[][][][] stk_should_not_add_candidate_position = new boolean[512][9][9][10];
    int[] scaler = new int[30];
    double current_difficulty;
    int focus_x = -1, focus_y = -1;
    ImageButton[] sudogeneroption_btns = new ImageButton[5];
    boolean generation_task_on = false;
    static boolean newly_generated = true;
    ImageButton[] sudotool_btns = new ImageButton[8];
    Random rander = new Random();
    ice_sudoku_core sudo_player = new ice_sudoku_core();
    int penmode = 1;
    int thbgid = 0;
    int timecounter = 0;
    int techid;

    int[] greenpoint = new int[3];
    int[][] redpoint = new int[50][3];
    int[][] bluepoint = new int[50][3];
    int green_num = -1;
    int blue_num = -1;
    int red_num = -1;
    int need_auto_deal = 0;
    boolean tint_on = false;
    boolean need_reflush = false;
    int canpos_offset;
    int settingnum=2;
    int []already_settings=new int[2];
    boolean auto_update_candidate=true;
    boolean use_extra_link=true;
    boolean analysis_board_on=false;
    String tech_str="";
    String extra_str="";
    stepper step_player = new stepper();
    Timer timer;
    int total_unit;
    int total_height, total_width, bigblocksizer;
    static final int nThreads = Runtime.getRuntime().availableProcessors();
    int thread_num = Math.min(nThreads*2, 20);
    SudokuGenerationTask[] sudotasks = new SudokuGenerationTask[20];
    int aim_difficulty;
    Vector<Object> seeds_vec = new Vector<>();
    ExecutorService fixedThreadPool = Executors.newCachedThreadPool();

    MyCanvas drawer;
    RelativeLayout sudolayout_outer;
    boolean canvas_showing;
    class solve_sudoku_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            clear_color_board();
            sudo_player.solvesudo_inthree(sudo, sudoext);
            display_sudoku(sudo);
        }
    }

    class save_sudoku_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            show_save_windows();
        }
    }
    class show_info_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            alertmessage_with(tech_str,extra_str);
        }
    }
    class show_clipboard_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            show_clipboard();
        }
    }
    class show_help_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            alertmessage_with("ice sudoku alpha 0.1 帮助文档 ","左上角雪花按钮: 生成新数独\n 右下角灯泡按钮: 分步求解 \n⚡️按钮: 求解数独 \n显微镜按钮: 逻辑分析当前数独\n\n点击扫帚按钮，然后输入你见到的数独，最后点击显微镜按钮即可导入外部数独并自动评估其等级\n\n 如果两次点击一个相同格自动切换主选数候选数输入 \n\n分步求解的详细技巧信息点击(i)按钮即可查看 \n\n ice sudoku 由中山大学计科王子诚独立维护 \n\n");
        }
    }
    class read_sudoku_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            show_read_windows();
        }
    }

    class analysis_sudoku_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            boolean importer = false;
            if(sudo_player.spacevacant(sudoext,80)==1){
                sudo_player.copysudo(sudoext,player_ans);
                importer=true;
            }
            current_difficulty = sudo_player.all_scale_analysis(sudoext, scaler, 0);
            show_scaler_text();
            if(importer){
                int [][][]nsudo = new int[10][9][9];
                sudo_player.copysudo(nsudo[0],sudoext);
                generating_finish(nsudo);
            }
        }
    }

    class pen_pencil_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (focus_x == -1)
                return;
            long ids = view.getId();
            int opt = (int) ids - 5000;
            penmode = opt ^ 1;
            update_pen_btns();
            flush_number_buttons();
        }
    }

    class next_step_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(!auto_update_candidate){
                alertmessage_with("⚠️","必须打开自动候选才能使用分步求解");
                return;
            }
            if(sudo_player.spacevacant(sudoext,66)==1){
                alertmessage_with("⚠️","数独至少有17个有效数字");
                return;
            }
            auto_deal_with_stepper();
            int[][][] temper = new int[10][9][9];
            techid = step_player.analysis_step_from(player_candidate, temper);
            flush_candidate_map_from_stepper();
        }
    }

    void auto_deal_with_stepper() {

        if (need_auto_deal == 1) {
            push_one_step();
            if (green_num != 0) {
                if (player_candidate[greenpoint[0]][greenpoint[1]][0] != greenpoint[2]) {
                    focus_x = greenpoint[0];
                    focus_y = greenpoint[1];
                    number_click_happened(greenpoint[2]);
                }
            }
            if (red_num != 0) {
                for (int i = 0; i < red_num; i++) {
                    if (player_candidate[redpoint[i][0]][redpoint[i][1]][redpoint[i][2]] == 0)
                        continue;
                    player_candidate[redpoint[i][0]][redpoint[i][1]][redpoint[i][2]] = 0;
                    candidatebutton[redpoint[i][0]][redpoint[i][1]][redpoint[i][2]].setVisibility(View.INVISIBLE);
                    should_not_add_candidate_position[redpoint[i][0]][redpoint[i][1]][redpoint[i][2]] = true;
                }
            }
            need_auto_deal = 0;
            clear_color_board();
        }
    }

    void flush_candidate_map_from_stepper() {
        String[] talks = {"", "Single Square 九宫格排除", "Single row 行排除", "Single col 列排除", "Single bit 单点排除"
                , "Hiddenpair 隐式对", "Pointing 简单指向", "Hiddentriple 隐式三联", "Nakedpair 显式对", "Nakedtriple 显式三联"
                , "X-wing X链", "Swordfish 剑鱼", "XY-wing XY链", "XYZ-wing XYZ链", "Unique-Rectangle type 1 唯一矩形"
                , "Unique-Rectangle type 2 唯一矩形", "Unique-Rectangle type 3 唯一矩形", "Unique-Rectangle type 4 唯一矩形"
                , "Quad 四联", "Jellyfish 水母形态", "Anti-Pointing 反指向", "Forcing-Chain 强链", "Infer 推断", "Strong-Infer 强力推断", "Mega-Infer 超级推断"
                , "Dynamic-Infer 动态推断", "Turbot-Fish 比目鱼"};
        setTitle(talks[techid]);


        green_num = 0;
        blue_num = 0;
        red_num = 0;
        int back = 0;
        int[][][] temp_bits = new int[10][9][9];
        tint_on = true;
        step_player.get_paint_map(temp_bits);
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                back = temp_bits[0][i][j];
                for (int p = 1; p < 10; p++) {

                    candidatebutton[i][j][p].setTextColor(Color.rgb(0, 0, 0));
                    candidatebutton[i][j][p].setBackgroundColor(originbackgroudcolor);

                    if (player_candidate[i][j][0] != 0) {
                        continue;
                    }

                    if (player_candidate[i][j][p] != 1)
                        continue;

                    switch (temp_bits[p][i][j]) {
                        case 0:
                            candidatebutton[i][j][p].setTextColor(Color.rgb(0, 0, 0));
                            candidatebutton[i][j][p].setBackgroundColor(originbackgroudcolor);
                            break;
                        case 1:
                            // NSLog(@"attention to %d,%d val %d",i,j,p);
                            //  [sudobutton[i][j] setImage:[NSImage imageNamed:@"greychecker_gre"]];
                            back = 1;

                            candidatebutton[i][j][p].setTextColor(Color.rgb(255, 255, 255));
                            candidatebutton[i][j][p].setBackgroundColor(getResources().getColor(R.color.stepgreen));

                            greenpoint[0] = i;
                            greenpoint[1] = j;
                            greenpoint[2] = p;
                            green_num++;

                            break;
                        case 2:
                            // [sudobutton[i][j] setImage:[NSImage imageNamed:@"greychecker_lorg"]];
                            if (back != 1)
                                back = 2;
                            candidatebutton[i][j][p].setTextColor(getResources().getColor(R.color.lightred));
                            candidatebutton[i][j][p].setBackgroundColor(getResources().getColor(R.color.stepred));
                            redpoint[red_num][0] = i;
                            redpoint[red_num][1] = j;
                            redpoint[red_num][2] = p;
                            red_num++;

                            break;
                        case 3:
                            if (back == 0)
                                back = 3;
                            candidatebutton[i][j][p].setTextColor(getResources().getColor(R.color.lightblue));
                            candidatebutton[i][j][p].setBackgroundColor(getResources().getColor(R.color.stepblue));

                            bluepoint[blue_num][0] = i;
                            bluepoint[blue_num][1] = j;
                            bluepoint[blue_num][2] = p;
                            blue_num++;

                            break;
                        case 4:
                            if (back == 0)
                                back = 3;
                            // [sudobutton[i][j] setImage:[NSImage imageNamed:@"greychecker_blu"]];
                            candidatebutton[i][j][p].setTextColor(Color.rgb(255, 255, 255));
                            candidatebutton[i][j][p].setBackgroundColor(getResources().getColor(R.color.linker_blue));

                            bluepoint[blue_num][0] = i;
                            bluepoint[blue_num][1] = j;
                            bluepoint[blue_num][2] = p;
                            blue_num++;

                            break;
                        default:
                            break;
                    }
                }


                switch (back) {
                    case 0:
                        sudobuttons[i][j].setBackgroundColor(originbackgroudcolor);
                        break;
                    case 1:
                        sudobuttons[i][j].setBackgroundColor(getResources().getColor(R.color.dimorange));
                        break;
                    case 2:
                        sudobuttons[i][j].setBackgroundColor(getResources().getColor(R.color.dimred));
                        break;
                    case 3:
                        sudobuttons[i][j].setBackgroundColor(getResources().getColor(R.color.backblue));
                        break;
                    default:
                        sudobuttons[i][j].setBackgroundColor(originbackgroudcolor);
                        break;
                }


            }
        show_tech_detail();
        if (techid >= 22) {
            drawing_routes();
        }
        need_auto_deal = 1;
//    [self Write_analysis_report];
        need_reflush = true;

    }
    
    void show_tech_detail(){
         tech_str="";
         extra_str="";
        boolean []number_exist=new boolean[10];
        int three_blue=0;

        for(int q=0;q<blue_num;q++)
        {
            number_exist[bluepoint[q][2]]=true;
        }
        for(int q=1;q<10;q++)
        {
            if(number_exist[q])
            {
                if(q!=bluepoint[1][2]&&q!=bluepoint[0][2])
                {
                    three_blue=q;
                    break;
                }
            }
        }

        switch (techid) {
            case 0:
                tech_str="求解完毕 😂";
                break;
            case 1:
                tech_str+=String.format("%d 在九宫格唯一",greenpoint[2]);
                setTitle(tech_str);
                break;
            case 2:
                tech_str+=String.format("%d 在这一行唯一",greenpoint[2]);
                setTitle(tech_str);
                break;
            case 3:
                tech_str+=String.format("%d 在这一列唯一",greenpoint[2]);
                break;
            case 4:
                tech_str+=String.format("这个格只有%d",greenpoint[2]);
                break;
            case 5:
                tech_str+=String.format("[Hidden pair] 隐藏对(%d,%d)",bluepoint[0][2],bluepoint[1][2]);
                extra_str=String.format("可以看出在某行、列、九宫格内只有这两个格存在%d,%d,所以可以删掉除了%d,%d之外的候选数",bluepoint[0][2],bluepoint[1][2],bluepoint[0][2],bluepoint[1][2]);
                break;
            case 6:
                tech_str+=String.format("[Pointing] 指向(%d)",redpoint[0][2]);
                extra_str=String.format("可以看出%d在九宫格内仅存在于同一行或同一列,因此在该行或该列的其他%d可以删去",bluepoint[0][2],bluepoint[0][2]);
                break;
            case 7:
                tech_str+=String.format("[Hidden triple] (%d,%d,%d)",bluepoint[0][2],bluepoint[1][2],three_blue);
                extra_str=String.format("可以看出在某行、列、九宫格内只有这三个格存在%d,%d,%d,所以可以删掉除了%d,%d,%d之外的候选数",bluepoint[0][2],bluepoint[1][2],three_blue,bluepoint[0][2],bluepoint[1][2],three_blue);
                break;
            case 8:
                tech_str+=String.format("[Naked pair] 两格仅有(%d,%d)",bluepoint[0][2],bluepoint[1][2]);
                extra_str=String.format("两格共同所在某行、列、九宫格中,其余所有的%d,%d都可以删去",bluepoint[0][2],bluepoint[1][2]);
                break;
            case 9:
                tech_str+=String.format("[Naked triple] 三格仅有(%d,%d,%d)",bluepoint[0][2],bluepoint[1][2],three_blue);
                extra_str=String.format("三格共同所在某行、列、九宫格,其余所有的%d,%d,%d都可以删去",bluepoint[0][2],bluepoint[1][2],bluepoint[2][2]);
                break;
            case 10:
                tech_str+=String.format("[X-wing] %d在行%d,%d列%d,%d交叉点",bluepoint[0][2],bluepoint[0][1],bluepoint[3][1],bluepoint[0][0],bluepoint[3][0]);
                extra_str=String.format("其中两行或两列的%d只在交叉点上存在,意味着4个交叉点上的%d一定存在两个,那么可以删去不在交叉点上的%d",bluepoint[0][2],bluepoint[0][2],bluepoint[0][2]);
                break;
            case 11:
                tech_str+="[Swordfish]剑鱼图形";
                extra_str=String.format("其中三行或三列的%d只在交叉点上存在,意味着9个交叉点上的%d一定存在三个,那么可以删去不在交叉点上的%d",bluepoint[0][2],bluepoint[0][2],bluepoint[0][2]);
                break;
            case 12:
                tech_str+=String.format("[XY-Wing] (%d)",redpoint[0][2]);
                extra_str=String.format("可以看出无论目标格取什么值,蓝色候选数%d其中一个会生效,导致橙色候选数%d被删去",bluepoint[0][2],redpoint[0][2]);
                break;
            case 13:
                tech_str+=String.format("[XYZ-Wing] (%d)",redpoint[0][2]);
                extra_str=String.format("可以看出无论目标格取什么值,蓝色候选数%d其中一个会生效,导致橙色候选数%d被删去",bluepoint[0][2],redpoint[0][2]);
                break;
            case 14:
                tech_str+=String.format("[Unique Rectangle type 1] (%d,%d)",bluepoint[0][2],bluepoint[1][2]);
                extra_str=String.format("矩形4格内只存在2个相同候选数会导致多解而该矩形中只有一个格有超过2个候选数,所以可以删去该格的%d,%d避免多解",bluepoint[0][2],bluepoint[1][2]);
                break;
            case 15:
                tech_str+=String.format("[Unique Rectangle type 2] (%d,%d)",bluepoint[0][2],bluepoint[1][2]);
                extra_str=String.format("矩形4格内只存在2个相同候选数会导致多解而该矩形中有2个格拥有除了%d,%d的候选数，并且都是%d,因此这两个格内的%d存在才能避免多解情况,通过这一点可以排除掉该行或该列的其他%d",bluepoint[0][2],bluepoint[1][2],redpoint[0][2],redpoint[0][2],redpoint[0][2]);
                break;
            case 16:
                tech_str+=String.format("[Unique Rectangle type 3] (%d,%d)",bluepoint[0][2],bluepoint[1][2]);
                extra_str=String.format("矩形4格内只存在2个相同候选数会导致多解,矩形中2个格有超过2个候选数,且在同一行或同一列,而这两个格除了%d,%d以外的候选数无论怎么存在都会删去橙色候选数",bluepoint[0][2],bluepoint[1][2]);
                break;
            case 17:
                tech_str+=String.format("[Unique Rectangle type 4] (%d,%d)",bluepoint[0][2],bluepoint[1][2]);
                extra_str=String.format("矩形4格内只存在2个相同候选数%d,%d会导致多解,矩形中2个格有超过2个候选数,同时其中一个候选数构成了X-wing,因此可以删去这两格中的另外一个候选数",bluepoint[0][2],bluepoint[1][2]);
                break;
            case 18:
                tech_str+="[Quad] 四个格里仅有四种数字";
                break;
            case 19:
                tech_str+="[Jelly fish] 四行四列构成了水母图形";
                break;
            case 20:
                tech_str+=String.format("[Anti-pointing] 反指向\n");
                extra_str=String.format("该行或该列所有的%d都在这个九宫格里,所以该九宫格的其他%d不存在",redpoint[0][2],redpoint[1][2]);
                break;
            case 21:
                tech_str+=String.format("[Forcing-Chain] 强链 (%d)\n",redpoint[0][2]);
                extra_str=link_check_report();
                break;
            case 22:
                tech_str+=String.format("[Infer] 推断 (%d)\n",redpoint[0][2]);
                extra_str=link_check_report();
                break;
            case 23:
                tech_str+=String.format("[Strong-Infer] 强力推断 (%d)\n",redpoint[0][2]);
                extra_str=link_check_report();
                break;
            case 24:
                tech_str+=String.format("[Mega-Infer] 超级推断 (%d)\n",redpoint[0][2]);
                extra_str=link_check_report();
                break;
            case 25:
                tech_str+=String.format("[Dynamic-Infer] 动态推断(%d)\n",redpoint[0][2]);
                extra_str=link_check_report();
                break;
            default:
                tech_str+=String.format("下一步太难解释了，直接看答案吧");
                sudo_player.solvesudo_inthree(sudo, sudoext);
                display_sudoku(sudo);
                break;
        }
        sudotool_btns[6].setVisibility(View.VISIBLE);
        if(analysis_board_on){
            setTitle(tech_str);
            analysis_board.setText(extra_str);
        }
    }
    String link_check_report(){
        String linkrep="";
        int []linecheckmod=new int[3];
        step_player.get_type_aimed_del(linecheckmod);
        linkrep+=String.format("如果候选数(%d)在橙色格存在",redpoint[0][2]);
        if(linecheckmod[0]==0)
        {
            linkrep+=",那么蓝色格全部候选数会被删空";
        }
        else{
            switch (linecheckmod[0]) {
                case 1:
                    linkrep+=String.format(",那么蓝色列中的全部%d被删除",linecheckmod[1]);
                    break;
                case 2:
                    linkrep+=String.format(",那么蓝色行中的全部%d被删除",linecheckmod[1]);
                    break;
                case 3:
                    linkrep+=String.format(",那么蓝色九宫格中的全部%d被删除",linecheckmod[1]);
                    break;
                default:
                    break;
            }
        }
        linkrep+=String.format(",所以橙色的(%d)可以安全删除",redpoint[0][2]);
        return linkrep;
    }

    void show_scaler_text() {
        String analysistext = "";
        String[] talks = {"", "Single Square 九宫格排除", "Single row 行排除", "Single col 列排除", "Single bit 单点排除"
                , "Hiddenpair 隐式对", "Pointing 简单指向", "Hiddentriple 隐式三联", "Nakedpair 显式对", "Nakedtriple 显式三联"
                , "X-wing X链", "Swordfish 剑鱼", "XY-wing XY链", "XYZ-wing XYZ链", "Unique-Rectangle type 1 唯一矩形"
                , "Unique-Rectangle type 2 唯一矩形", "Unique-Rectangle type 3 唯一矩形", "Unique-Rectangle type 4 唯一矩形"
                , "Quad 四联", "Jellyfish 水母形态", "Anti-Pointing 反指向", "Forcing-Chain 强链", "Infer 推断", "Strong-Infer 强力推断", "Mega-Infer 超级推断"
                , "Dynamic-Infer 动态推断", "Turbot-Fish 比目鱼"};
        if (current_difficulty > 0.9) {
            if (scaler[29] != -1) {
                String temp = String.format("Single 隐藏点 x %d\n", scaler[1] + scaler[2] + scaler[3] + scaler[4]);
                analysistext += temp;
                int list_len = 0;
                for (int i = 5; i < 29; i++) {
                    if (scaler[i] > 0)
                        list_len++;
                }
                for (int i = 5; i < 29; i++) {
                    if (list_len > 10 && current_difficulty > 7.1 && i < 10)
                        continue;
                    if (scaler[i] > 0) {
                        temp = String.format("%s x %d\n", talks[i], scaler[i]);
                        analysistext += temp;
                    }
                }
            } else {
                analysistext = "过于困难,解释失败,点击⚡️直接求解";
            }
        } else {
            if (current_difficulty < -1.5) {
                analysistext = "❌ 无解数独";
            } else {
                analysistext = "⚠️ 多解数独";
            }
        }
        String tit = String.format("难度 %.1f", current_difficulty);
        alertmessage_with(tit, analysistext);
        setTitle(tit);
    }

    void alertmessage_with(String title, String message) {
        AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                .setTitle(title)//标题
                .setMessage(message)//内容
                .create();
        alertDialog1.show();
    }

    String get_formatted_time() {
        SimpleDateFormat dfdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return dfdate.format(new Date());
    }

    void push_one_step() {
        int rpos = stkid & 511;
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                for (int p = 0; p < 10; p++) {
                    stk_player_candidate[rpos][i][j][p] = player_candidate[i][j][p];
                    stk_should_not_add_candidate_position[rpos][i][j][p] = should_not_add_candidate_position[i][j][p];
                }
        stkid++;

    }

    void withdraw_one_step() {
        if (stkid <= 0)
            return;
        stkid--;

        int rpos = stkid & 511;

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                for (int p = 0; p < 10; p++) {
                    player_candidate[i][j][p] = stk_player_candidate[rpos][i][j][p];
                    sudo[p][i][j] = stk_player_candidate[rpos][i][j][p];
                    should_not_add_candidate_position[i][j][p] = stk_should_not_add_candidate_position[rpos][i][j][p];
                }
        sudo_player.copysudo(player_ans, sudo[0]);
        display_sudoku(sudo);
    }

    void show_save_windows() {
        String[] saves = new String[31];
        final String[] update_strs = new String[31];
        for (int i = 0; i < 31; i++) {
            update_strs[i] = "空";
        }
        final String timepacksavename = "ICESAVEUPDATETIMES";
        final String timepackstr = ReadFile(timepacksavename);
        if (timepackstr != null) {
            String[] pieces = timepackstr.split(",");
            for (int i = 0; i < pieces.length && i < 31; i++) {
                update_strs[i] = pieces[i];
            }
        }
        for (int i = 0; i < 31; i++) {
            String savename = "取消";
            if (i != 0)
                savename = String.format("存档 %d [%s]", i, update_strs[i]);
            saves[i] = savename;
        }
        String tit = "保存到?";

        AlertDialog alertDialog4 = new AlertDialog.Builder(this).setTitle(tit).setItems(saves, new DialogInterface.OnClickListener() {//添加列表
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i != 0) {
                    String savename = String.format("ICESAVEPACK%d", i);
                    boolean res = true;
                    res = save_all_to(savename);
                    if (res) {
                        update_strs[i] = get_formatted_time();
                        StringBuilder packer = new StringBuilder();
                        for (int fi = 0; fi < 31; fi++) {
                            packer.append(update_strs[fi]).append(",");
                        }
                        WriteFile(timepacksavename, packer.toString());
                    }
                }
            }
        }).create();
        alertDialog4.show();
    }

    void show_read_windows() {
        String[] saves = new String[31];
        final String[] update_strs = new String[31];
        for (int i = 0; i < 31; i++) {
            update_strs[i] = "空";
        }
        final String timepacksavename = "ICESAVEUPDATETIMES";
        final String timepackstr = ReadFile(timepacksavename);
        if (timepackstr != null) {
            String[] pieces = timepackstr.split(",");
            for (int i = 0; i < pieces.length && i < 31; i++) {
                update_strs[i] = pieces[i];
            }
        }
        for (int i = 0; i < 31; i++) {
            String savename = "取消";
            if (i != 0)
                savename = String.format("存档 %d [%s]", i, update_strs[i]);
            saves[i] = savename;
        }
        String tit = "读取?";

        AlertDialog alertDialog5 = new AlertDialog.Builder(this).setTitle(tit).setItems(saves, new DialogInterface.OnClickListener() {//添加列表
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i != 0) {
                    String savename = String.format("ICESAVEPACK%d", i);
                    boolean res = true;
                    res = read_all_from(savename);
                    if (res) {
                        update_strs[i] = get_formatted_time();
                        StringBuilder packer = new StringBuilder();
                        for (int fi = 0; fi < 31; fi++) {
                            packer.append(update_strs[fi]).append(",");
                        }
                        WriteFile(timepacksavename, packer.toString());
                    }
                }
            }
        }).create();
        alertDialog5.show();
    }

    void show_generating_windows() {
        if (generation_task_on) {
            alertmessage_with("⚠️", "你已经开启数独生成任务");
            return;
        }
        final String[] items3 = new String[]{"☀️新手", "🌤简单", "☁️中等", "🌧困难", "⚡️顶尖","❄️ 冰数独 ","🎖 超一流"};//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(this).setTitle("选择生成数独难度").setItems(items3, new DialogInterface.OnClickListener() {//添加列表
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i<5){
                    generate_sudoku_for_difficulty(i + 1);
                }
                else{
                    if(i==5)
                        show_ice_sudoku_window();
                    else
                        show_world_class_window();
                }

            }
        }).create();
        alertDialog3.show();
    }
    void show_ice_sudoku_window(){
        String[]ice_packs = {"取消",
        "150000003004000502060070009500706300702004100006010000000320000320008700005000031",
        "001500200200001070407060001004700900009600080000894015070000040920000006105000800",
        "003000482000200009420098051005007010000000600701020000004000506000070003100305020",
        "900610700001000020080072000010400058400060009300080204000700000860030000005840030",
        "700001920040200071210000460490700000020409036003800090000320050050000000002007000",
        "820010034005000000403060050600100380501908000004030019000790800008400100000000093",
        "010200000325007008904000000100805070200010083070000000630500004000302906000004030",
        "010830290002004300003020008080040052509080030006010000300071000070090100000300720",
        "300100587805003000000500006400051768508006001006800200000040090050018000700900000",
        "006000002080070060900000010060000900709400000200006030603910048070380196000060000",
        "008079046070400001000003087020080000001000000086902030064300590100800000000006003",
        "070060090003709061000001200010020430090100000704090000000080740400006002057012000",
        "904800007005000069070509040063200000090045000507300000000100024050082700000930800",
        "008006023010020090020905701780300009000001000241000000000500040004000902830009006",
        "200041096090000100006009000000490003030050008600002000360020041007003800000060237",
        "040800020002000600360010004000080000800100005200300400130608500600079001027030006",
        "000001008400009000030875000025100047040650000103040600010000580700004030004010200",
        "400038120003004070000760403001090034009800007200470900700100000054300000010000090",
        "050002860000000201042010000001500030400000005625007008004060300013079006000103002",
        "800200003090003000300070102200010000040002006003040508020900061400020700001500200",
        "200000076360000800000670000405206080000710040070400200050800400030000720004100058",
        "000006900008000007030040006007060130210003000006001004602030071090000000000128609",
        "000400019007090458000500602000000500326700000090304000060009080008040007900870000",
        "000010002000000943000002186080400005300005000005090800009040078760800000048703009",
        "000409003546200000003000604000006008000308760605090302254900000708000030000000040",
        "000310800030002001010009070980506000004190006000000050098000000300900500520070490",
        "020080000890730002100090750400000010609008005010006000000000420000060087908027001",
        "940080310102000608000006200060001020208009400000000006407600000000200080020057904",
        "400000013120038000003100000001609000002070000900000701309050000510803002080000045",
        "000800052040070010000051700000000001900008007300507000670000900490360108083000046",
        "090100000005072104300500000000700000038009001400850002043607009000000600860900037",
        "007015002005007800100200000050000071002056980080700000000400230000093700003502006",
        "507000060400006500000000984001302058024050000050001400100007005000200003000038100",
        "702300100000000007030170090000025008927010000005006700510007060400001809200040071",
        "090800046800600900000700002062000004300400000500060001030271050600504020000000407",
        "438900000106000920000100004510800706060470000847050000600080002090006030300000600",
        "000020590001040000009800400000090085000207000070508030040302060260000310093080200",
        "507800020030967008084250000300040002012600000006000090000005007100720503000000210",
        "010002860060180070200700103700008200000000007826000490052001000470006000080090000",
        "004067501350900006700400080040200050000040100020701093500004000009070004430000070",
        "409600700000009841003070960006503100005007000300060000908100020260000000000042000",
        "680900400300050100009700603004093200210070030000600000090000300000308060003500004",
        "000471960090000008400980070005004007340000516002600000000260001200000700030008609",
        "007840903000000008008079000006032400400600300000000006060290030010000594900005620",
        "480000750203000060010000082020016005700008200040000000162800000000750000070021800",
        "005020300003806070008503200070900008000007930509060700900031000000600043004005000",
        "000008090090001508238050007500600320010000700003010900000000050800002000076045800",
        "060020007000001560037900000400002300002000008650070040000098001700000085820500400",
        "002060004806400103090800200200005039901000008003000400000040000000008075030906040",
        "070002009204000300001400700009003000000020054010540900000030090300074008020109003",
        "400800007900400006003000000052007030096000020004000160000006070037980601040003200",
        "200309506507010300000040081000004000809700100070003068900000800000001035056000000",
        "600000003000018060000000020500041008030006000400503006214709080008000070060080209",
        "172500803090000000508000700000050930000000008083007051030020086001300009050100340",
        "000000000020300905000061740000006007007004030900000450000105824854020000200640500",
        "070080006003009007002704090230000000140002070800900020300000160000090000008056042",
        "310004000075100400204070001020009060000003510000400900700250000400000100006040052",
        "060481000080007001009020000800092050670000008000708002000004503006009800400870100",
        "000300070400007908075800060007900040600078009049126000700041000001000304900080000",
        "320750000007600004008900000000006430046830500030000086003080200000000801089002043",
        "090080000001703000030060075600310709059000600000695080007130004080000010900000300",
        "009000040001000000420800600000005270090237004000610890046090080000003400050000901",
        "002006000005000064800003500008900000040060800120000070004071903003600000290430050",
        "590000102407901380120000000004080030802009004000400070006200000205008000780340020",
        "703500806080000005540000230007040020000805900090300000070050000004009000035200170",
        "800007390100060005030050080900500820000700904010000003760823000001000030000071000",
        "023000000700014000040300807000007062050020030200000000032070986019206000670930000",
        "080030900000508400506401070200005700008000090050109002020803060800007009307050000",
        "730510008081030000009460000000020005002070600095006720000000800028001040500200000",
        "080040070001800000740000809168050000000084001430019000000402705076000020000570090",
        "700800043000000900003600050000000000950104007400789500501070000300900065020350100",
        "050790800008103006030040000000200009000070160000460005040000020702504000005900607",
        "300209000079103000000000000058000200031000060296018005010300800640821000000007601",
        "060400320100060000007000004410905000700000000096730000950600070000010906600070403",
        "026003000530040200008900003003001907090704000070000604000000090007050400360410008",
        "820500003000016009000300400060001200010905070000040091001000508080150900359000007",
        "400630700007208000003000009500146000000003400048927600000800005890060170020400900",
        "005000010006000002002809500020680000608070009010020000001032080009508040080090003",
        "046830000000500100305000400460700051020000040050008609094007008000060900002900064",
        "000001700000000302509027000605100070000050009003004600064702000010005007007030901",
        "060000000070001800201690300000060710006007003030409006010002000594006000082050097",
        "900015400000030068000260905004000070000580000590001000003052090450000200029608050",
        "004000000090000007060204900002608000080000023400900800046302008830040260005100000",
        "900800600000020030001006700029000305600400000007000064000094003045008900300570400",
        "600030057003006000040002093460070000900060002032900700010020570000007300300100028",
        "169040350540000000000950000901060002600003090400100600020030400800704000094000006",
        "200030810005000630030400007002000078010000400504067003400200900000050300006001002",
        "000052600903000000060000092002890704000006501000040000390400006470620000006009008",
        "000410080000007001301000400496030000000002048200000130002065004105000006900080050",
        "001050084000000060846000750400100000500020008100500600625300001000070006780000200",
        "920001000006800050000093602760239000489060000500000060200010700000900004000080096",
        "003000000052004306600308070080900000004000980036010002400032800008600000360001000",
        "034006090650800000002030060000004000400260005001700024500340016000000002010600070",
        "006002050005760000120059000250904300000020507000800020900207100010500070503100002",
        "080020406020604085600500000000000010009016002170802650700000900000069500008105000",
        "100000400970002060004716000409370000700000300050008970002000003000800040007024680",
        "090041053040600000000093040458007201000000590000510078600000020012400000000080005",
        "200903500304008029905204060001000800000041030000080005008007000400800600000000298",
        "400070080009000007075008006010009603900060200006830004390005060000400009500096000",
        "680003090003910000200080506000000049900000200002600000040300000520094080300062001"};

        for(int i=1;i<ice_packs.length;i++){
            ice_packs[i]=String.format("🥶%d #",i)+ice_packs[i];
        }
        final String[]finpack = ice_packs;
        String tit = "❄️ ice sudoku ";

        AlertDialog alertDialog6 = new AlertDialog.Builder(this).setTitle(tit).setItems(finpack, new DialogInterface.OnClickListener() {//添加列表
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i != 0) {
                    String []pieces = finpack[i].split("#");
                    String itm=pieces[1];
                    for(int ic=0;ic<81;ic++){
                        int x=ic%9;
                        int y=ic/9;
                        sudo[0][x][y]=itm.charAt(ic)-'0';
                    }
                    generating_finish(sudo);
                }
            }
        }).create();
        alertDialog6.show();

    }

    void show_world_class_window(){
        String[]ice_packs = {"取消",
        "500000300020100070008000009040007000000821000000600010300000800060004020009000005",
        "800000009040001030007000600000023000050904020000105000006000700010300040900000008",
        "000070100000008050020900003530000000062000004094600000000001800300200009000050070",
        "000006080000100200009030005040070003000008010000200600071090000590000004804000000",
        "000056000050109000000020040090040070006000300800000002300000008002000600070500010",
        "500000004080006090001000200070308000000050000000790030002000100060900080400000005",
        "070200009003060000400008000020900010800004000006030000000000600090000051000700002",
        "100080000005900000070002000009500040800010000060007200000000710000004603030000402",
        "000900100000080007004002050200005040000100900000070008605000030300006000070000006",
        "000001080030500200000040006200300500000008010000060004050000700300970000062000000",
        "800000005040003020007000100000004000090702060000639000001000700030200040500000008",
        "900000001030004070006000200050302000000060000000078050002000600040700030100000009",
        "500000008030007040001000900020603000000725000000800060009000100070400030800000005",
        "400000009070008030006000100050702000000065000000003020001000600080300070900000004",
        "100006009007080030000200400000500070300001002000090600060003050004000000900020001",
        "800000001050009040003000600070056000000980000000704020006000300090400050100000008",
        "010000009005080700300700060004250000000000000000840200008007500600000030090000001",
        "300000005020007040001000900080036000000028000000704060009000100070400020500000003",
        "400000003080002060007000900010508000000701000000026050009000700020600080300000004",
        "600005020040700000009080000010000302000000087000200104070400003500006000008090000",
        "007002000500090400010600000400050003060100000002007000000000810900000306000080059",
        "000007090000800400003060001420010000031000002605000000060400800500020006000009070",
        "000600001000020400300009050090005030000040200000100006570008000002000000080000090",
        "006003000900080200070400000003006000040700000800020090500000008000000709000510020",
        "010300000000009000000710050004050900200000006070800030600000002080030070009000400",
        "000008070000300200005040009260094000059000006401000000000200300100060004000007080",
        "000800300000010005004002070200007040000300807000050001907000060600009000050000000",
        "800000007040001030009000600000532000050108020000400000006000900010300040700000008",
        "400000008050002090001000600070503000000060000000790030006000100020900050800000004",
        "300000009010006050002000400070060000000701000000845070004000200060500010900000003",
        "000000789000100036009000010200030000070004000008500100300020000005800090040007000",
        "100000000006700020080030500007060030000500008000004900300800600002090070040000001",
        "700000005040001030002000900060008000000946000000103080009000200010300040500000007",
        "001020000300004000050600070080900005002003000400010000070000038000800069000000200",
        "007580000000030000000076005400000020090000100003060008010600900006800003200000040",
        "097000000301005000045000800003008400000020060000100009700004300000900001000060020",
        "003700000050004000100020080900000012000000400080010090007300000200090006040005000",
        "000000100600000874000007026030400000005090000100008002009050000200001008040300000",
        "100000004020006090005000800030650000000372000000098070008000500060900020400000001",
        "005300000800000020070010500400005300010070006003200080060500009004000030000009700",
        "000002005006700400000009008070090000600400700010000080060300100300000002400005000",
        "020000600400080007009000010005006000300040900010200000000700004000001050800090300",
        "900000007030008040006000200010389000000010000000205010002000600080400030700000009",
        "002400006030010000500008000007000002010000030900600400000007001000090080400200500",
        "100300000020090400005007000800000100040000020007060003000400800000020090006005007",
        "002600000030080000500009100006000002080000030700001400000004005010020080000700900",
        "003500100040080000600009000800000002050700030001000400000006009000020080070100500",
        "300000906040200080000060000050800020009000307000007000010042000000000010508100000",
        "000090050010000030002300700004500070800000200000006400090010000080060000005400007",
        "100500000200000030004060100006007000008000009400080200000009007040010600000005003"};

        for(int i=1;i<ice_packs.length;i++){
            ice_packs[i]=String.format("🎖%d #",i)+ice_packs[i];
        }
        final String[]finpack = ice_packs;
        String tit = "🏅超一流 ";

        AlertDialog alertDialog6 = new AlertDialog.Builder(this).setTitle(tit).setItems(finpack, new DialogInterface.OnClickListener() {//添加列表
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i != 0) {
                    String []pieces = finpack[i].split("#");
                    String itm=pieces[1];
                    for(int ic=0;ic<81;ic++){
                        int x=ic%9;
                        int y=ic/9;
                        sudo[0][x][y]=itm.charAt(ic)-'0';
                    }
                    generating_finish(sudo);
                }
            }
        }).create();
        alertDialog6.show();

    }

    void load_settings(){
        String settings_str = ReadFile("IceSudokuCustomSettings");
        int []settings={1,1};
        if(settings_str!=null){
            for(int i=0;i<settingnum&&i<settings_str.length();i++){
                settings[i]=settings_str.charAt(i)-'0';
            }
        }
        for(int j=0;j<2;j++){
            already_settings[j]=settings[j];
//            System.out.printf("Setting %d is %d\n",j,already_settings[j]);
        }

    }
    void sure_or_not_clear_all(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("❗️确定清空全部?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               clear_board();
               for(int i=0;i<9;i++)
                   for(int j=0;j<9;j++)
                       sudoext[i][j]=0;
            }
        });
        builder.setNegativeButton("取消",null);

        builder.show();
    }
    void show_clipboard(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("剪切板");
        final EditText editor = new EditText(MainActivity.this);
        editor.setText(pack_sudoku(sudoext));

        final String[] items = new String[]{"读取","取消"};
        builder.setPositiveButton("读取", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               boolean res = release_sudoku_from(sudoext,editor.getText().toString());
               if(res){
                    sudo_player.clear_bits(sudo);
                    sudo_player.copysudo(sudo[0],sudoext);
                    generating_finish(sudo);
               }
               else{
                   alertmessage_with("❌","不是81位数独格式");
               }

            }
        });
        builder.setNegativeButton("取消",null);
        builder.setView(editor);
        builder.show();
    }
    void show_setting_choices() {

        final String[] on_off = new String[]{"🟩⬜ ️️","⬜🟥️ "};
        String[]shower=new String[]{"自动填充候选数", "候选数高亮显示"};
        for(int i=0;i<settingnum;i++){
            if(already_settings[i]==1){
                shower[i]=on_off[0]+shower[i];
            }
            else{
                shower[i]=on_off[1]+shower[i];
            }
        }
        final String[] items3 = shower;//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(this).setTitle("设置").setIcon(R.mipmap.ice).setItems(items3, new DialogInterface.OnClickListener() {//添加列表
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(already_settings[i]==0){
                    already_settings[i]=1;
                }
                else{
                    already_settings[i]=0;
                }
                String newsettingstr = "";
                for(int ic=0;ic<settingnum;ic++){
                    newsettingstr+=String.format("%d",already_settings[ic]);
                }
                WriteFile("IceSudokuCustomSettings",newsettingstr);
                apply_settings();

            }
        }).create();
        alertDialog3.show();
    }

    void apply_settings(){
        auto_update_candidate = already_settings[0] == 1;
        use_extra_link = already_settings[1] == 1;
        if(!auto_update_candidate){
            for(int i=0;i<9;i++)
                for(int j=0;j<9;j++){
                    for(int p=1;p<10;p++){
                        player_candidate[i][j][p]=0;
                        candidatebutton[i][j][p].setVisibility(View.INVISIBLE);
                    }
                }
        }
        else{
            display_sudoku(sudo);
        }
    }

    class setting_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            show_setting_choices();
        }
    }

    class del_undo_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            long ids = view.getId();
            int opt = (int) ids - 5100;
            clear_color_board();
            setTitle("");
            if (opt == 0) {
                if (focus_x != -1)
                    number_click_happened(0);
            } else if (opt == 1) {
                withdraw_one_step();
            }
        }
    }

    void number_click_happened(int num) {
        push_one_step();
        if (penmode == 1) {
            if (sudoext[focus_x][focus_y] != 0)
                return;
            player_candidate[focus_x][focus_y][0] = num;
            player_ans[focus_x][focus_y] = num;
            sudo_player.clear_bits(sudo);
            sudo_player.copysudo(sudo[0], player_ans);
            sudo_player.low_build_bit(sudo);
            display_sudoku(sudo);
        } else {
            player_candidate[focus_x][focus_y][num] ^= 1;
            if (player_candidate[focus_x][focus_y][num] == 1) {
                should_not_add_candidate_position[focus_x][focus_y][num] = false;
                candidatebutton[focus_x][focus_y][num].setVisibility(View.VISIBLE);
            } else {
                should_not_add_candidate_position[focus_x][focus_y][num] = true;
                candidatebutton[focus_x][focus_y][num].setVisibility(View.INVISIBLE);
            }
        }
        autosave();
        flush_number_buttons();
    }

    class number_button_clicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (focus_x != -1) {
                int num = (int) view.getId() - 4000;
                clear_color_board();
                number_click_happened(num);
            }
        }
    }
    class clear_all_act implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            setTitle("");
            sure_or_not_clear_all();
        }
    }
    class sudo_main_button_clicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (focus_x != -1) {
                sudobuttons[focus_x][focus_y].setBackgroundColor(originbackgroudcolor);
            }

            int nowid = view.getId();
            int nfocus_x = nowid / 100;
            int nfocus_y = (nowid % 100) / 10;
            if (nfocus_x == focus_x && nfocus_y == focus_y) {
                penmode ^= 1;
                update_pen_btns();
            }
            if(focus_x!=-1)
                if(sudo[0][focus_x][focus_y]!=0){
                    clear_link(sudo[0][focus_x][focus_y]);
                }
            focus_x = nfocus_x;
            focus_y = nfocus_y;
            if (sudo[0][focus_x][focus_y] != 0)
                extra_link(sudo[0][focus_x][focus_y]);
            flush_number_buttons();
            sudobuttons[focus_x][focus_y].setBackgroundColor(focusbackgroudcolor);

//            String clickedmessage = String.format("%d is clicked",view.getId());
//            Toast.makeText(MainActivity.this,clickedmessage,Toast.LENGTH_SHORT).show();
        }
    }

    long get_nano_sec() {
        long pre = System.nanoTime() & 2147483647;
        return pre;
    }

    void update_pen_btns() {
        if (penmode == 1) {
            penpencilbtns[0].setImageDrawable(getResources().getDrawable(R.mipmap.pen));
            penpencilbtns[1].setImageDrawable(getResources().getDrawable(R.mipmap.penciloff));
        } else {
            penpencilbtns[0].setImageDrawable(getResources().getDrawable(R.mipmap.penoff));
            penpencilbtns[1].setImageDrawable(getResources().getDrawable(R.mipmap.pencil));
        }
    }

    private class SudokuGenerationTask implements Runnable {
        ice_sudoku_core realgener = new ice_sudoku_core();
        int[][][] temp_sudo = new int[10][9][9];
        boolean isfirst = true;

        public void run() {
            if (newly_generated) {
                return;
            }

            realgener.init();
            long threadId = Thread.currentThread().getId();
            long seeder = threadId + get_nano_sec() + rander.nextInt(100);
            realgener.set_seed(seeder);
            isfirst = false;

            int res = realgener.generatesudo_max_try(temp_sudo, aim_difficulty);
            if (res == 1 && !newly_generated) {
                newly_generated = true;
                sudo_player.set_turn(1);
                realgener.bitcopysudo(sudo, temp_sudo);
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }
    }


    void generate_sudoku_for_difficulty(int real_difficuly) {
        generation_task_on = true;
        clear_board();
        int[][][] nsudo = new int[10][9][9];
        sudo_player.clear_bits(nsudo);
        sudo_player.clear_bits(sudo);
        sudo_player.set_turn(0);
        if (real_difficuly == 1) {
            ice_sudoku_core.taskover = false;
            int res = sudo_player.generatesudo_max_try(nsudo, real_difficuly);
            while (res == 0) {
                res = sudo_player.generatesudo_max_try(nsudo, real_difficuly);
            }
            generating_finish(nsudo);
            ice_sudoku_core.taskover = true;
        } else {
            seeds_vec.clear();
            setTitle("生成数独中 ...");
            thbgid = 0;
            aim_difficulty = real_difficuly;
            newly_generated = false;
            ice_sudoku_core.taskover = false;
            for (int p = 0; p < thread_num; p++) {
                fixedThreadPool.execute(sudotasks[p]);
                thbgid++;
            }
            timecounter = 0;
        }
    }

    class sudo_generation_clicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//            int real_difficuly = view.getId();
//            real_difficuly-=1999;
//            generate_sudoku_for_difficulty(real_difficuly);

            show_generating_windows();
        }
    }

    class sudo_gener_stop_clicked implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//            int real_difficuly = view.getId();
//            real_difficuly-=1999;
//            generate_sudoku_for_difficulty(real_difficuly);
            Message msg = new Message();
            msg.what = 3;
            handler.sendMessage(msg);
        }
    }

    void generating_finish(int[][][] nsudo) {
        sudo_player.copysudo(sudoext, nsudo[0]);
        sudo_player.low_build_bit(nsudo);
        display_sudoku(nsudo);
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                sudobuttons[i][j].setBackgroundColor(originbackgroudcolor);
            }
        for (int p = 0; p < 10; p++)
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    player_candidate[i][j][p] = nsudo[p][i][j];
        sudo_player.copysudo(player_ans, nsudo[0]);
        sudogeneroption_btns[2].setVisibility(View.INVISIBLE);
        generation_task_on = false;
        ice_sudoku_core.taskover = true;
        stkid = 0;
        autosave();
    }

    void autosave() {
        save_all_to("auto");
    }

    boolean save_all_to(String savename) {
        boolean res = save_to_pack(savename, sudoext, player_ans, player_candidate, should_not_add_candidate_position);
        return res;
    }

    boolean read_all_from(String savename) {
        boolean res = read_from_pack(savename, sudoext, player_ans, player_candidate, should_not_add_candidate_position);
        if (!res)
            return false;
        sudo_player.copysudo(sudo[0], player_ans);
        sudo_player.low_build_bit(sudo);
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                for (int p = 1; p < 10; p++) {
                    if (sudo[p][i][j] == 1) {
                        if (should_not_add_candidate_position[i][j][p])
                            sudo[p][i][j] = 0;
                        else {
                            player_candidate[i][j][p] = 1;
                        }
                    }
                }
        display_sudoku(sudo);
        return true;
    }

    void autoread() {
        read_all_from("auto");
    }

    @Override
    public Window getWindow() {
        return super.getWindow();
    }

    @Override
    public WindowManager getWindowManager() {
        return super.getWindowManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sudo_player.init();
        sudo_player.set_seed((int) sudo_player.get_nano_sec());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("ice sudoku");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_view();
    }

    void clear_board() {
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                player_ans[i][j] = 0;
                sudo[0][i][j] = 0;
                for (int p = 0; p < 10; p++) {
                    should_not_add_candidate_position[i][j][p] = false;
                    player_candidate[i][j][p] = 0;
                }
            }
        clear_color_board();
        sudo_player.clear_bits(sudo);
        display_sudoku(sudo);
    }

    void clear_color_board() {
        if (canvas_showing) {
            clear_canvas_view();
            canvas_showing = false;
        }
        if(analysis_board_on){
            analysis_board.setText("");
        }
        setTitle("");
        if (green_num == -1)
            return;

        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                sudobuttons[i][j].setBackgroundColor(originbackgroudcolor);

        for (int i = 0; i < red_num; i++) {
            candidatebutton[redpoint[i][0]][redpoint[i][1]][redpoint[i][2]].setBackgroundColor(originbackgroudcolor);
            candidatebutton[redpoint[i][0]][redpoint[i][1]][redpoint[i][2]].setTextColor(Color.BLACK);
            sudobuttons[redpoint[i][0]][redpoint[i][1]].setBackgroundColor(originbackgroudcolor);
            for (int j = 0; j < 3; j++) {
                redpoint[i][j] = 0;
            }
        }
        for (int i = 0; i < blue_num; i++) {
            candidatebutton[bluepoint[i][0]][bluepoint[i][1]][bluepoint[i][2]].setBackgroundColor(originbackgroudcolor);
            candidatebutton[bluepoint[i][0]][bluepoint[i][1]][bluepoint[i][2]].setTextColor(Color.BLACK);
            sudobuttons[bluepoint[i][0]][bluepoint[i][1]].setBackgroundColor(originbackgroudcolor);
            for (int j = 0; j < 3; j++) {
                bluepoint[i][j] = 0;
            }
        }
        if (candidatebutton[greenpoint[0]][greenpoint[1]][greenpoint[2]] != null) {
            candidatebutton[greenpoint[0]][greenpoint[1]][greenpoint[2]].setBackgroundColor(originbackgroudcolor);
            candidatebutton[greenpoint[0]][greenpoint[1]][greenpoint[2]].setTextColor(Color.BLACK);
        }

        for (int j = 0; j < 3; j++) {
            greenpoint[j] = 0;
        }

        if(sudotool_btns[6]!=null){
            sudotool_btns[6].setVisibility(View.INVISIBLE);
        }

        green_num = 0;
        blue_num = 0;
        red_num = 0;
    }

    void flush_number_buttons() {
        if (penmode == 1) {
            for (int i = 0; i < 9; i++) {
                numbuttons[i].setBackgroundColor(numbernormalcolor);
                numbuttons[i].setTextColor(Color.rgb(0, 0, 0));
            }
        } else {
            for (int p = 0; p < 9; p++) {
                int nm = p + 1;
                if (player_candidate[focus_x][focus_y][nm] == 1) {
                    numbuttons[p].setBackgroundColor(numberfocuscolor);
                    numbuttons[p].setTextColor(Color.rgb(244, 244, 244));
                } else {
                    numbuttons[p].setBackgroundColor(numbernormalcolor);
                    numbuttons[p].setTextColor(Color.rgb(0, 0, 0));
                }
            }
        }
    }

    void flush_candidate_map(int[][][] sudo) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudo[0][i][j] != 0) {
                    for (int p = 1; p < 10; p++) {
                        candidatebutton[i][j][p].setVisibility(View.INVISIBLE);
                        player_candidate[i][j][p] = 0;
                    }
                } else {
                    for (int p = 1; p < 10; p++) {
                        if (sudo[p][i][j] == 1 && !should_not_add_candidate_position[i][j][p]) {
                            candidatebutton[i][j][p].setVisibility(View.VISIBLE);
                            player_candidate[i][j][p] = 1;
                        } else {
                            candidatebutton[i][j][p].setVisibility(View.INVISIBLE);
                            player_candidate[i][j][p] = 0;
                        }
                    }
                }
            }
        }
    }

    void clear_link(int num) {
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
            {
                if(sudo[0][i][j]==num){
                    sudobuttons[i][j].setBackgroundColor(originbackgroudcolor);
                }
                if(use_extra_link){
                    if(player_candidate[i][j][num]==1){
                        candidatebutton[i][j][num].setBackgroundColor(originbackgroudcolor);
                    }
                }
            }
    }
    void extra_link(int num){

        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
            {
                if(sudo[0][i][j]==num){
                    sudobuttons[i][j].setBackgroundColor(focusbackgroudcolor);
                }
                if(use_extra_link){
                    if(player_candidate[i][j][num]==1){
                        candidatebutton[i][j][num].setBackgroundColor(getResources().getColor(R.color.line));
                    }
                }
            }
    }
    void display_sudoku(int [][][]sudo){
        String allstr = "";
        setTitle("");
        if(analysis_board_on){
            analysis_board.setText("");
        }
        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){
                String shower="";
                if(sudo[0][i][j]!=0){
                    shower=String.format("%d",sudo[0][i][j]);
                    allstr+=shower;
                    if(sudoext[i][j]!=0)
                        sudobuttons[i][j].setTextColor(Color.rgb(0, 0, 0));
                    else
                        sudobuttons[i][j].setTextColor(getResources().getColor(R.color.stepgreen_dk));
                }
                else{
                    sudobuttons[i][j].setAlpha(0.5f);
                    allstr+="0";
                }
                sudobuttons[i][j].setText(shower);
            }
        }
        if(auto_update_candidate)
            flush_candidate_map(sudo);
        //Toast.makeText(MainActivity.this,allstr,Toast.LENGTH_SHORT).show();
    }
    void init_view()
    {
        this.getApplicationContext().getFileStreamPath("FileName.xml")
            .getPath();
        rander.setSeed(get_nano_sec());
        step_player.init();
        for(int i=0;i<thread_num;i++){
            sudotasks[i]=new SudokuGenerationTask();
        }
        RelativeLayout sudolayout = new RelativeLayout(this);
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        total_width = dm.widthPixels;
        total_height = dm.heightPixels;
        int blocklinepad =(int)(0.02f*total_width);
        int simplelinepad = (int)(0.008f*total_width);
        bigblocksizer=total_width/10;
        total_unit = total_height / bigblocksizer;
        System.out.printf("%d , %d\n",total_width,total_height);
        int bignumsize = 32;
        int smallnumsize = 13;
        load_settings();
        if(total_width<1000){
            smallnumsize=10;
            bignumsize=26;
        }


        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++)
            {
                int iv = i/3;
                int jv = j/3;
                int ider = iv+jv;
                sudobuttons[i][j]=new Button(this);
                sudobuttons[i][j].setId(i*100+j*10);
                sudobuttons[i][j].setText("");
                sudobuttons[i][j].setPadding(0,0,0,0);
                sudobuttons[i][j].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                sudobuttons[i][j].setTextSize(bignumsize);
                sudobuttons[i][j].setOnClickListener(new sudo_main_button_clicked());
                sudobuttons[i][j].setBackgroundColor(originbackgroudcolor);
                sudobuttons[i][j].setAlpha(0.5f);
                RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(bigblocksizer,bigblocksizer);
                btParams.leftMargin = i*bigblocksizer+iv*blocklinepad+i*simplelinepad;
                btParams.topMargin= total_height - (14-j)*bigblocksizer - bigblocksizer/3*2 +jv*blocklinepad+j*simplelinepad;
                sudobuttons[i][j].setLayoutParams(btParams);
                largebutton_params[i][j]=btParams;

                int subwidth = bigblocksizer/3;
                canpos_offset = subwidth/2;
                for(int p=1;p<10;p++){
                    int subx = (p-1)%3;
                    int suby = (p-1)/3;
                    candidatebutton[i][j][p]=new TextView(this);
                    RelativeLayout.LayoutParams smallParams = new RelativeLayout.LayoutParams(subwidth,subwidth);
                    smallParams.leftMargin = btParams.leftMargin+subx*subwidth;
                    smallParams.topMargin = btParams.topMargin+suby*subwidth;
                    candidate_positions[i][j][p][0]=smallParams.leftMargin + canpos_offset;
                    candidate_positions[i][j][p][1]=smallParams.topMargin + canpos_offset;
                    candidatebutton[i][j][p].setLayoutParams(smallParams);
                    candidatebutton[i][j][p].setVisibility(View.VISIBLE);
                    candidatebutton[i][j][p].setEnabled(false);
                    candidatebutton[i][j][p].setIncludeFontPadding(false);
                    candidatebutton[i][j][p].setBackgroundColor(originbackgroudcolor);
//                    candidatebutton[i][j][p].setTypeface(null, Typeface.SANS_SERIF.);

                    candidatebutton[i][j][p].setPadding(0,0,0,0);
                    candidatebutton[i][j][p].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    candidatebutton[i][j][p].setTextColor(Color.rgb(0,0,0));
                    String subtitle = String.format("%d",p);
                    candidatebutton[i][j][p].setText(subtitle);
                    candidatebutton[i][j][p].setTextSize(smallnumsize);
                    candidatebutton[i][j][p].setGravity(Gravity.CENTER);

                    sudolayout.addView(candidatebutton[i][j][p]);
                }
                sudolayout.addView(sudobuttons[i][j]);
            }

        Object []difficulty_text= {R.mipmap.liv1,R.mipmap.liv2,R.mipmap.liv3,R.mipmap.liv4,R.mipmap.liv5};
         int difficulty_btn_top_position = total_height - 16*bigblocksizer - bigblocksizer/3;
         if(difficulty_btn_top_position<0)
             difficulty_btn_top_position=0;

         if(difficulty_btn_top_position>100){
             analysis_board = new TextView(this);
             RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(total_width,difficulty_btn_top_position);
             btParams.leftMargin=0;
             btParams.topMargin=0;
             analysis_board.setPadding(10,10,10,10);
             analysis_board.setLayoutParams(btParams);
             analysis_board_on=true;
             analysis_board.setTextSize(16);

             analysis_board.setBackgroundColor(originbackgroudcolor);
             sudolayout.addView(analysis_board);
         }


        for(int q=0;q<3;q++){
            sudogeneroption_btns[q]=new ImageButton(this);
            sudogeneroption_btns[q].setId(2000+q);
            sudogeneroption_btns[q].setPadding(0,0,0,0);



            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(bigblocksizer,bigblocksizer);
            btParams.leftMargin=bigblocksizer*q;

            if(q==0)
            {
                sudogeneroption_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.snow));
                sudogeneroption_btns[q].setOnClickListener(new sudo_generation_clicked());
            }
            else if(q==2){
                sudogeneroption_btns[q].setOnClickListener(new sudo_gener_stop_clicked());
                sudogeneroption_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.stop));
                sudogeneroption_btns[q].setVisibility(View.INVISIBLE);
            }
            else if(q==1){
                sudogeneroption_btns[q].setOnClickListener(new show_help_act());
                sudogeneroption_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.help));
            }

            btParams.topMargin= difficulty_btn_top_position;
            sudogeneroption_btns[q].setLayoutParams(btParams);

            sudolayout.addView(sudogeneroption_btns[q]);
        }


        for(int q=0;q<7;q++){
            sudotool_btns[q]=new ImageButton(this);

            sudotool_btns[q].setId(3000+q);
//            sudotool_btns[q].setText(tool_text[q]);
            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(bigblocksizer,bigblocksizer);
            btParams.leftMargin = total_width - bigblocksizer*(q+1);
            btParams.topMargin= difficulty_btn_top_position;
            sudotool_btns[q].setLayoutParams(btParams);
            if(q==0){
                sudotool_btns[q].setOnClickListener(new solve_sudoku_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.lightning));
            }
            else if(q==1){
                sudotool_btns[q].setOnClickListener(new analysis_sudoku_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.statistics));
            }
            else if(q==2){
                sudotool_btns[q].setOnClickListener(new save_sudoku_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.save));
            }
            else if(q==3){
                sudotool_btns[q].setOnClickListener(new read_sudoku_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.open));
            }
            else if(q==4){
                sudotool_btns[q].setOnClickListener(new show_clipboard_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.clipboard));
            }
            else if(q==5){
                sudotool_btns[q].setOnClickListener(new clear_all_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.clean));
            }
            else if(q==6){
                sudotool_btns[q].setOnClickListener(new show_info_act());
                sudotool_btns[q].setImageDrawable(getResources().getDrawable(R.mipmap.info));
                sudotool_btns[q].setVisibility(View.INVISIBLE);
            }

            sudolayout.addView(sudotool_btns[q]);
        }

        int numbutton_per_height = bigblocksizer;
        int numbutton_per_width = total_width/5;
        for(int i=0;i<9;i++){
            String displaystr = String.format("%d",i+1);
            int subx = i%3;
            int suby = i/3;
            numbuttons[i]=new Button(this);
            numbuttons[i].setId(4001+i);
            numbuttons[i].setText(displaystr);
            numbuttons[i].setPadding(0,0,0,0);
            numbuttons[i].setTextSize(24);
            numbuttons[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            numbuttons[i].setBackgroundColor(Color.rgb(200,200,200));
            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(numbutton_per_width,numbutton_per_height-5);
            btParams.leftMargin = subx*numbutton_per_width+5*subx + total_width/5;
            btParams.topMargin= total_height - numbutton_per_height*(4-suby) - (int)(0.03f*total_height);
            numbuttons[i].setLayoutParams(btParams);
            numbuttons[i].setOnClickListener(new number_button_clicked());
            sudolayout.addView(numbuttons[i]);
        }

        for(int q=0;q<3;q++)
        {
            penpencilbtns[q]=new ImageButton(this);
            penpencilbtns[q].setId(5000+q);
            penpencilbtns[q].setPadding(0,0,0,0);
            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(numbutton_per_width/5*4,numbutton_per_height);
            btParams.leftMargin = 0;
            btParams.topMargin= total_height - numbutton_per_height*(4-q) - (int)(0.03f*total_height);;
            penpencilbtns[q].setLayoutParams(btParams);
            if(q!=2){
                penpencilbtns[q].setOnClickListener(new pen_pencil_act());
            }
            else{
                penpencilbtns[q].setOnClickListener(new setting_act());
                penpencilbtns[q].setImageDrawable(getResources().getDrawable(R.mipmap.setting));
            }
            sudolayout.addView(penpencilbtns[q]);
        }
        update_pen_btns();

        for(int q=0;q<3;q++)
        {
            delundobtns[q]=new ImageButton(this);
            delundobtns[q].setId(5100+q);
            delundobtns[q].setPadding(0,0,0,0);
            if(q==0){
                delundobtns[q].setImageDrawable(getResources().getDrawable(R.mipmap.delete));
            }
            else if(q==1){
                delundobtns[q].setImageDrawable(getResources().getDrawable(R.mipmap.undo));
            }
            else{
                delundobtns[q].setImageDrawable(getResources().getDrawable(R.mipmap.idea));
            }
            RelativeLayout.LayoutParams btParams = new RelativeLayout.LayoutParams(numbutton_per_width/5*4,numbutton_per_height);
            btParams.leftMargin = total_width/5*4 + numbutton_per_width/5;
            btParams.topMargin= total_height - numbutton_per_height*(4-q) - (int)(0.03f*total_height);;
            delundobtns[q].setLayoutParams(btParams);
            if(q!=2){
                delundobtns[q].setOnClickListener(new del_undo_act());
            }
            else{
                delundobtns[q].setOnClickListener(new next_step_act());
            }

            sudolayout.addView(delundobtns[q]);
        }
        drawer = new MyCanvas(this);
        drawer.setPaintDefaultStyle();
        int [][]routes = new int[300][4];
        routes[0][0]=0;
        routes[0][1]=0;
        routes[0][2]=total_width/2;
        routes[0][3]=total_height/2;

        routes[1][0]=total_width/2;
        routes[1][1]=total_height/2;
        routes[1][2]=total_width;
        routes[1][3]=0;

        drawer.init_routes(routes,2);
        sudolayout_outer = sudolayout;
        sudolayout.addView(drawer);

        this.setContentView(sudolayout);
        apply_settings();
        autoread();
        timer = new Timer();
        timer.schedule(new TimerTask() {

            // TimerTask 是个抽象类,实现的是Runable类
            @Override
            public void run() {
                if(!newly_generated){
                    Message msg = new Message();
                    msg.what=2;
                    handler.sendMessage(msg);
                }
            }

        }, 1000, 1000);

        clear_canvas_view();
    }

    void drawing_routes(){
        int [][]cont = new int[300][3];
        step_player.get_route(cont);
        int totalroute = cont[299][0];
        int [][]routes = new int[300][4];
        if(totalroute<=0)
            return;
        routes[0][0]=candidate_positions[redpoint[0][0]][redpoint[0][1]][redpoint[0][2]][0];
        routes[0][1]=candidate_positions[redpoint[0][0]][redpoint[0][1]][redpoint[0][2]][1];
        routes[0][2]=candidate_positions[cont[totalroute-1][0]][cont[totalroute-1][1]][cont[totalroute-1][2]][0];
        routes[0][3]=candidate_positions[cont[totalroute-1][0]][cont[totalroute-1][1]][cont[totalroute-1][2]][1];

        if(routes[0][2]>routes[0][0]){
            routes[0][2]-=canpos_offset;
            routes[0][0]+=canpos_offset;
        }
        else if(routes[0][2]<routes[0][0]){
            routes[0][2]+=canpos_offset;
            routes[0][0]-=canpos_offset;
        }
        else{
            if(routes[0][3]>=routes[0][1]){
                routes[0][2]-=canpos_offset;
            }
            else{
                routes[0][2]+=canpos_offset;
            }
        }

        if(routes[0][3]>routes[0][1]){
            routes[0][3]-=canpos_offset;
            routes[0][1]+=canpos_offset;
        }
        else if(routes[0][3]<routes[0][1]){
            routes[0][3]+=canpos_offset;
            routes[0][1]-=canpos_offset;
        }
        else{
            if(routes[0][2]>routes[0][0]){
                routes[0][3]-=canpos_offset;
            }
            else if(routes[0][2]<routes[0][0]){
                routes[0][3]+=canpos_offset;
            }
        }
        int ic=1;
        for(int i=totalroute-1;i>0;i--,ic++){
            int j=i-1;
            routes[ic][0]=candidate_positions[cont[i][0]][cont[i][1]][cont[i][2]][0];
            routes[ic][1]=candidate_positions[cont[i][0]][cont[i][1]][cont[i][2]][1];
            routes[ic][2]=candidate_positions[cont[j][0]][cont[j][1]][cont[j][2]][0];
            routes[ic][3]=candidate_positions[cont[j][0]][cont[j][1]][cont[j][2]][1];
            if(routes[ic][2]>routes[ic][0]){
                routes[ic][2]-=canpos_offset;
                routes[ic][0]+=canpos_offset;
            }
            else if(routes[ic][2]<routes[ic][0]){
                routes[ic][2]+=canpos_offset;
            }
            else{
                if(routes[ic][3]>routes[ic][1]){
                    routes[ic][2]-=canpos_offset;
                }
                else{
                    routes[ic][2]+=canpos_offset;
                }
            }
            if(routes[ic][3]>routes[ic][1]){
                routes[ic][3]-=canpos_offset;
                routes[ic][1]+=canpos_offset;
            }
            else if(routes[ic][3]<routes[ic][1]){
                routes[ic][3]+=canpos_offset;
            }
            else{
                if(routes[ic][2]>routes[ic][0]){
                    routes[ic][3]-=canpos_offset;
                }
                else{
                    routes[ic][3]+=canpos_offset;
                }
            }
        }
        drawer = new MyCanvas(this);
        drawer.setPaintDefaultStyle();
        drawer.init_routes(routes,ic);
        sudolayout_outer.addView(drawer);
        canvas_showing=true;
    }
    void clear_canvas_view(){
        sudolayout_outer.removeView(drawer);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                generating_finish(sudo);
            }
            else if(msg.what==2){
                timecounter++;
                if(timecounter==3){
                    sudogeneroption_btns[2].setVisibility(View.VISIBLE);
                }
                int i = rander.nextInt(9);
                int j = rander.nextInt(9);
                int r = rander.nextInt(255);
                int g = rander.nextInt(255);
                int b = rander.nextInt(255);
                sudobuttons[i][j].setBackgroundColor(Color.rgb(r,g,b));
            }
            else if(msg.what==3){
                sudo_player.clear_bits(sudo);
                newly_generated=true;
                generation_task_on=false;
                generating_finish(sudo);
            }
        }
    };


    public class MyCanvas extends View {

        private Canvas myCanvas;
        private Paint myPaint=new Paint();
        int [][]routes=new int[300][4];
        int routelen=0;

        public void init_routes(int [][]newroutes,int len){
            routelen = len;
            for(int i=0;i<routelen;i++) {
                for(int j=0;j<4;j++){
                    routes[i][j]=newroutes[i][j];
                }
            }
        }

        public MyCanvas(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        public MyCanvas(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            // TODO Auto-generated constructor stub
        }

        public MyCanvas(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            this.myCanvas=canvas;
            if(total_width<1000)
                myPaint.setStrokeWidth(6);
            else
                myPaint.setStrokeWidth(10);
            for(int i=0;i<routelen;i++){
                myPaint.setColor(Color.argb(192,122+rander.nextInt(99),122+rander.nextInt(99),122+rander.nextInt(99)));
                drawAL(routes[i][0],routes[i][1],routes[i][2],routes[i][3]);
            }
        }

        /**
         * 设置画笔默认样式
         */
        public void setPaintDefaultStyle(){
            myPaint.setAntiAlias(true);
            myPaint.setColor(getResources().getColor(R.color.line));
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);
        }


        /**
         * 画圆
         * @param x x坐标
         * @param y	y坐标
         * @param radius	圆的半径
         */
        public void drawCircle(float x,float y,float radius){
            myCanvas.drawCircle(x, y, radius, myPaint);
            invalidate();
        }

        /**
         * 画一条直线
         * @param fromX 起点x坐标
         * @param fromY	起点Y坐标
         * @param toX	终点X坐标
         * @param toY	终点Y坐标
         */
        public void drawLine(float fromX,float fromY,float toX,float toY){
            Path linePath=new Path();
            linePath.moveTo(fromX, fromY);
            linePath.lineTo(toX, toY);
            linePath.close();
            myCanvas.drawPath(linePath, myPaint);
            invalidate();
        }


        /**
         * 画箭头
         * @param sx
         * @param sy
         * @param ex
         * @param ey
         */
        public void drawAL(int sx, int sy, int ex, int ey)
        {
            double H = 8; // 箭头高度
            double L = 6; // 底边的一半
            int x3 = 0;
            int y3 = 0;
            int x4 = 0;
            int y4 = 0;
            double awrad = Math.atan(L / H); // 箭头角度
            double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
            double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
            double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
            double x_3 = ex - arrXY_1[0]; // (x3,y3)是第一端点
            double y_3 = ey - arrXY_1[1];
            double x_4 = ex - arrXY_2[0]; // (x4,y4)是第二端点
            double y_4 = ey - arrXY_2[1];
            Double X3 = Double.valueOf(x_3);
            x3 = X3.intValue();
            Double Y3 = Double.valueOf(y_3);
            y3 = Y3.intValue();
            Double X4 = Double.valueOf(x_4);
            x4 = X4.intValue();
            Double Y4 = Double.valueOf(y_4);
            y4 = Y4.intValue();
            // 画线
            myCanvas.drawLine(sx, sy, ex, ey,myPaint);
            Path triangle = new Path();
            triangle.moveTo(ex, ey);
            triangle.lineTo(x3, y3);
            triangle.lineTo(x4, y4);
            triangle.close();
            myCanvas.drawPath(triangle,myPaint);

        }
        // 计算
        public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen)
        {
            double mathstr[] = new double[2];
            // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
            double vx = px * Math.cos(ang) - py * Math.sin(ang);
            double vy = px * Math.sin(ang) + py * Math.cos(ang);
            if (isChLen) {
                double d = Math.sqrt(vx * vx + vy * vy);
                vx = vx / d * newLen;
                vy = vy / d * newLen;
                mathstr[0] = vx;
                mathstr[1] = vy;
            }
            return mathstr;
        }


    }

    public String pack_sudoku(int [][]sudo){
        StringBuilder res = new StringBuilder();
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++){
                String sub = String.format("%d",sudo[j][i]);
                res.append(sub);
            }
        return res.toString();
    }
    public boolean release_sudoku_from(int [][]sudo,String source){
        if(source.length()!=81)
            return false;
        for(int i=0;i<source.length();i++){
            int x=i%9;
            int y=i/9;
            int v=source.charAt(i)-'0';
            if(v>=0&&v<=9){
                sudo[x][y]=v;
            }
            else{
                return false;
            }
        }
        return true;
    }
    public String pack_9_9_10_sudoku(int [][][]sudo){
        StringBuilder res = new StringBuilder();
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++){
                for(int p=0;p<10;p++){
                    String sub = String.format("%d",sudo[i][j][p]);
                    res.append(sub);
                }
            }
        return res.toString();
    }
    public boolean release_9_9_10_sudoku_from(int [][][]sudo,String source){
        if(source.length()<810)
            return false;
        for(int i=0;i<source.length();i++){
            int x=i/90;
            int y=(i%90)/10;
            int z=i%10;
            int v=source.charAt(i)-'0';
            if(v>=0&&v<=9){
                sudo[x][y][z]=v;
            }
            else{
                return false;
            }
        }
        return true;
    }
    public String pack_9_9_10_sudoku(boolean [][][]sudo){
        StringBuilder res = new StringBuilder();
        for(int i=0;i<9;i++)
            for(int j=0;j<9;j++){
                for(int p=0;p<10;p++){
                    String sub = "0";
                    if(sudo[i][j][p])
                        sub="1";
                    res.append(sub);
                }
            }
        return res.toString();
    }
    public boolean release_9_9_10_sudoku_from(boolean [][][]sudo,String source){
        if(source.length()<810)
            return false;
        for(int i=0;i<source.length();i++){
            int x=i/90;
            int y=(i%90)/10;
            int z=i%10;
            int v=source.charAt(i)-'0';
            if(v>=0&&v<=9){
                sudo[x][y][z]= v != 0;
            }
            else{
                return false;
            }
        }
        return true;
    }

    public boolean save_to_pack(String packname,int [][]sudoext,int [][]player_ans,int [][][]player_candidate,boolean [][][]should_not_add_candidate_position){
        String aimstr = "";
        aimstr+=pack_sudoku(sudoext);
        aimstr+=",";
        aimstr+=pack_sudoku(player_ans);
        aimstr+=",";
        aimstr+=pack_9_9_10_sudoku(player_candidate);
        aimstr+=",";
        aimstr+=pack_9_9_10_sudoku(should_not_add_candidate_position);
        WriteFile(packname,aimstr);
        return true;
    }
    public boolean read_from_pack(String packname,int [][]sudoext,int [][]player_ans,int [][][]player_candidate,boolean [][][]should_not_add_candidate_position){
        String aimstr = ReadFile(packname);
        if(aimstr==null)
            return false;
        String []pieces = aimstr.split(",");
        boolean res = release_sudoku_from(sudoext,pieces[0]);
        if(!res)
            return false;
        res=release_sudoku_from(player_ans,pieces[1]);
        if(!res)
            return false;
        res=release_9_9_10_sudoku_from(player_candidate,pieces[2]);
        if(!res)
            return false;
        res=release_9_9_10_sudoku_from(should_not_add_candidate_position,pieces[3]);
        return res;
    }
    public String ReadFile(String fileName) {
        fileName+=".txt";
        File file = new java.io.File((this
                .getApplicationContext().getFileStreamPath(fileName)
                .getPath()));
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            StringBuilder result = new StringBuilder();
            String line = "";

            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    // 写入文件
    public void WriteFile(String fileName,String Content) {
        fileName+=".txt";
        File file = new java.io.File((this
                .getApplicationContext().getFileStreamPath(fileName)
                .getPath()));
        if(!file.exists()){
            try {
                boolean res = file.createNewFile();
                if(!res)
                    return;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(Content.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
