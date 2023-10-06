package com.example.quickarithmetic;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

class Problem {
    // a class that contains all the essential data of one arithmetic problem
    int first, second, answer, attempt = -1;
    String strProblem;
}

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // declare global variables
    EditText firstQuestion, secondQuestion, thirdQuestion, fourthQuestion, fifthQuestion;
    EditText firstAnswer, secondAnswer, thirdAnswer, fourthAnswer, fifthAnswer;
    Button btnStart, btnBack, btnEnter, btnQuit, btnBackspace;
    Button btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine, btnZero;
    SwitchCompat swImpossibleMode;
    ConstraintLayout startInterface, playInterface;
    Problem[] test = new Problem[100];
    Boolean start = true, impossibleAccess, saveImpossible, completeImpossible;
    long startTime;
    SharedPreferences save;
    SharedPreferences.Editor edit;
    String first, second, third, timeImp;
    TextView firstP, secondP, thirdP, timeImpossible;
    Timer timer;
    TimerTask task;
    int max = 13;
    int num = 0;
    private Vibrator vibrator;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up ad
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // set up vibrator
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        setTitle("");
        // retrieve the data of the leaderboard
        save = PreferenceManager.getDefaultSharedPreferences(this);
        edit = save.edit();
        first = save.getString("firstP", "empty");
        second = save.getString("secondP", "empty");
        third = save.getString("thirdP", "empty");
        timeImp = save.getString("timeImp", "empty");
        // retrieve data related to impossible mode
        impossibleAccess = save.getBoolean("<60", false);
        saveImpossible = save.getBoolean("impossibleState", false);
        completeImpossible = save.getBoolean("legend", false);
        // show the leaderboard if it isn't empty
        firstP = findViewById(R.id.firstPlace);
        secondP = findViewById(R.id.secondPlace);
        thirdP = findViewById(R.id.thirdPlace);
        timeImpossible = findViewById(R.id.impossibleLeaderboard);
        setLeaderboard();
        // assign constraintLayouts
        startInterface = findViewById(R.id.start);
        (playInterface = findViewById(R.id.play)).setVisibility(View.INVISIBLE);
        // assign OnClickListeners to the buttons
        (btnStart = findViewById(R.id.startBtn)).setOnClickListener(this);
        (btnBack = findViewById(R.id.backBtn)).setOnClickListener(this);
        (btnEnter = findViewById(R.id.enterBtn)).setOnClickListener(this);
        (btnQuit = findViewById(R.id.quitBtn)).setOnClickListener(this);
        (btnOne = findViewById(R.id.one_btn)).setOnClickListener(this);
        (btnTwo = findViewById(R.id.two_btn)).setOnClickListener(this);
        (btnThree = findViewById(R.id.three_btn)).setOnClickListener(this);
        (btnFour = findViewById(R.id.four_btn)).setOnClickListener(this);
        (btnFive = findViewById(R.id.five_btn)).setOnClickListener(this);
        (btnSix = findViewById(R.id.six_btn)).setOnClickListener(this);
        (btnSeven = findViewById(R.id.seven_btn)).setOnClickListener(this);
        (btnEight = findViewById(R.id.eight_btn)).setOnClickListener(this);
        (btnNine = findViewById(R.id.nine_btn)).setOnClickListener(this);
        (btnZero = findViewById(R.id.zero_btn)).setOnClickListener(this);
        (btnBackspace = findViewById(R.id.backspace)).setOnClickListener(this);

        // assign switch to OnClickListener and restore state of last app usage
        (swImpossibleMode = findViewById(R.id.impossible)).setOnClickListener(this);
        swImpossibleMode.setChecked(saveImpossible);
        // assign EditTexts
        (firstQuestion = findViewById(R.id.firstQ)).setEnabled(false);
        (firstAnswer = findViewById(R.id.firstA)).setEnabled(false);
        (secondQuestion = findViewById(R.id.secondQ)).setEnabled(false);
        (secondAnswer = findViewById(R.id.secondA)).setEnabled(false);
        (thirdQuestion = findViewById(R.id.thirdQ)).setEnabled(false);
        (thirdAnswer = findViewById(R.id.thirdA)).setEnabled(false);
        (fourthQuestion = findViewById(R.id.fourthQ)).setEnabled(false);
        (fourthAnswer = findViewById(R.id.fourthA)).setEnabled(false);
        (fifthQuestion = findViewById(R.id.fifthQ)).setEnabled(false);
        (fifthAnswer = findViewById(R.id.fifthA)).setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        // when a button or switch is pressed, vibrate the phone
        vibrator.vibrate(50);

        // direct the clicked button to its corresponding method
        if (v.getId() == R.id.startBtn || v.getId() == R.id.quitBtn) {
            if (v.getId() == R.id.quitBtn) timer.cancel(); // cancel timer since player quit the game
            change();
        }
        else if (v.getId() == R.id.backBtn) {
            num--;
            thirdAnswer.setText("");
            refresh("");
        }
        else if (v.getId() == R.id.enterBtn) {
            // user enters invalid input
            if (thirdAnswer.getText().toString().equals("") || thirdAnswer.getText().toString().length() > 5) {
                thirdAnswer.setText("");
                Toast.makeText(this, "Invalid input. Try again.", Toast.LENGTH_SHORT).show();
            }
            else {  // user enters something
                num++;
                String input = thirdAnswer.getText().toString();
                thirdAnswer.setText("");
                refresh(input);
            }
        }
        else if (v.getId() == R.id.impossible && impossibleAccess) {  // impossible mode switch is pressed and user has access
            saveImpossible = !saveImpossible;
            edit.putBoolean("impossibleState", saveImpossible);
            edit.apply();
        }
        else if (v.getId() == R.id.impossible) {    // impossible mode switch is pressed but user does not have access
            swImpossibleMode.setChecked(false);
            Toast.makeText(this, "Complete the fluency drill with 100% accuracy in less than a minute to unlock impossible mode", Toast.LENGTH_LONG).show();
        }
        else { // user pressed a number button
            String ans_builder;
            if (v.getId() == R.id.one_btn) ans_builder = thirdAnswer.getText().toString() + "1";
            else if (v.getId() == R.id.two_btn) ans_builder = thirdAnswer.getText().toString() + "2";
            else if (v.getId() == R.id.three_btn) ans_builder = thirdAnswer.getText().toString() + "3";
            else if (v.getId() == R.id.four_btn) ans_builder = thirdAnswer.getText().toString() + "4";
            else if (v.getId() == R.id.five_btn) ans_builder = thirdAnswer.getText().toString() + "5";
            else if (v.getId() == R.id.six_btn) ans_builder = thirdAnswer.getText().toString() + "6";
            else if (v.getId() == R.id.seven_btn) ans_builder = thirdAnswer.getText().toString() + "7";
            else if (v.getId() == R.id.eight_btn) ans_builder = thirdAnswer.getText().toString() + "8";
            else if (v.getId() == R.id.nine_btn) ans_builder = thirdAnswer.getText().toString() + "9";
            else if (v.getId() == R.id.zero_btn) ans_builder = thirdAnswer.getText().toString() + "0";
            else if (v.getId() == R.id.backspace && thirdAnswer.getText().toString().length() > 0) ans_builder = thirdAnswer.getText().toString().substring(0, thirdAnswer.getText().toString().length() - 1);
            else ans_builder = "";
            thirdAnswer.setText(ans_builder);
        }
    }

    public void create(Problem[] test) {
        // create 100 arithmetic problems
        for (int i = 0; i < 100; i++) {
            test[i] = new Problem();
            int opn = (int) (Math.random() * 4);
            test[i].first = (int) (Math.random() * max);
            do test[i].second = (int) (Math.random() * max);
            while ((opn == 3 && test[i].second == 0) || (opn == 1 && test[i].second > test[i].first));
            String op;
            if (opn == 0) {op = "+"; test[i].answer = test[i].first + test[i].second;}
            else if (opn == 1) {op = "-"; test[i].answer = test[i].first - test[i].second;}
            else if (opn == 2) {op = "*"; test[i].answer = test[i].first * test[i].second;}
            else {op = "/"; test[i].first *= test[i].second; test[i].answer = test[i].first / test[i].second;}
            test[i].strProblem = "" + test[i].first + " " + op + " " + "" + test[i].second + " = ";
        }
    }

    public void setLeaderboard() {
        // display the leaderboard
        String temp;
        if (!Objects.equals(first, "empty")) {
            temp = "1: " + "" + first + "s";
            System.out.println(temp);
            firstP.setText(temp);
            firstP.setVisibility(View.VISIBLE);
        } else firstP.setVisibility(View.INVISIBLE);
        if (!Objects.equals(second, "empty")) {
            temp = "2: " + "" + second + "s";
            secondP.setText(temp);
            secondP.setVisibility(View.VISIBLE);
        } else secondP.setVisibility(View.INVISIBLE);
        if (!Objects.equals(third, "empty")) {
            temp = "3: " + "" + third + "s";
            thirdP.setText(temp);
            thirdP.setVisibility(View.VISIBLE);
        } else thirdP.setVisibility(View.INVISIBLE);
        if (completeImpossible) {
            temp = "Fastest time in impossible mode: " + "" + timeImp + "s";
            timeImpossible.setText(temp);
            timeImpossible.setVisibility(View.VISIBLE);
        } else timeImpossible.setVisibility(View.INVISIBLE);
    }

    public void changeHelper() {
        // switch from the play constraint layout to the start constraint layout
        firstQuestion.setText("");
        secondQuestion.setText("");
        thirdQuestion.setText("");
        fourthQuestion.setText("");
        fifthQuestion.setText("");
        firstAnswer.setText("");
        secondAnswer.setText("");
        thirdAnswer.setText("");
        startInterface.setVisibility(View.VISIBLE);
        playInterface.setVisibility(View.INVISIBLE);
        setLeaderboard();
        start = !start;
    }

    public void change() {
        // switch between the play constraint layout and start constraint layout
        thirdAnswer.setText("");
        if (start) {
            startInterface.setVisibility(View.INVISIBLE);
            playInterface.setVisibility(View.VISIBLE);
            // create test
            num = 0;
            if (saveImpossible) max = 26;
            else max = 13;
            create(test);

            // create the timer
            timer = new Timer();
            task = new TimerTask() {
                public void run() {
                    // if the user does not finish in time, switch the user to the start constraint layout
                    runOnUiThread(() -> {
                        Toast.makeText(getBaseContext(), "Times up!", Toast.LENGTH_SHORT).show();
                        changeHelper();
                    });
                }
            };

            // if impossible mode is activated, set timer to 60s
            if (saveImpossible) timer.schedule(task, 60000);
            // if impossible mode is not activated, set timer to 600s
            else timer.schedule(task, 600000);

            // show the first problem
            refresh("");
            // start stopwatch
            startTime = System.nanoTime();
            start = !start;
        }
        else changeHelper();  // switch to the start constraint layout
    }

    public void refresh(String input) {
        String fst, snd;
        // assign the (n-1)th problem's attempt to the user's input
        if (!input.equals("")) test[num - 1].attempt = parseInt(input);
        // if user is playing on impossible mode and user input is wrong, kick the user out of the game
        if (saveImpossible && num != 0 && (test[num - 1].attempt != test[num - 1].answer)) {
            timer.cancel();
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
            change();
        }
        else {
            // display the questions according to the problem the user is on
            switch (num) {
                case 0: // user is on first problem
                    firstQuestion.setVisibility(View.INVISIBLE);
                    firstAnswer.setVisibility(View.INVISIBLE);
                    secondQuestion.setVisibility(View.INVISIBLE);
                    secondAnswer.setVisibility(View.INVISIBLE);
                    fourthQuestion.setVisibility(View.VISIBLE);
                    fourthAnswer.setVisibility(View.VISIBLE);
                    fifthQuestion.setVisibility(View.VISIBLE);
                    fifthAnswer.setVisibility(View.VISIBLE);
                    btnBack.setEnabled(false);
                    thirdQuestion.setText(test[0].strProblem);
                    fourthQuestion.setText(test[1].strProblem);
                    fifthQuestion.setText(test[2].strProblem);
                    break;
                case 1: // user is on second problem
                    firstQuestion.setVisibility(View.INVISIBLE);
                    firstAnswer.setVisibility(View.INVISIBLE);
                    secondQuestion.setVisibility(View.VISIBLE);
                    secondAnswer.setVisibility(View.VISIBLE);
                    btnBack.setEnabled(true);
                    snd = "" + test[0].attempt;
                    secondQuestion.setText(test[0].strProblem);
                    secondAnswer.setText(snd);
                    thirdQuestion.setText(test[1].strProblem);
                    fourthQuestion.setText(test[2].strProblem);
                    fifthQuestion.setText(test[3].strProblem);
                    break;
                case 98: // user is on the penultimate problem
                    fourthQuestion.setVisibility(View.VISIBLE);
                    fourthAnswer.setVisibility(View.VISIBLE);
                    fifthQuestion.setVisibility(View.INVISIBLE);
                    fifthAnswer.setVisibility(View.INVISIBLE);
                    fst = "" + test[96].attempt;
                    snd = "" + test[97].attempt;
                    firstQuestion.setText(test[96].strProblem);
                    firstAnswer.setText(fst);
                    secondQuestion.setText(test[97].strProblem);
                    secondAnswer.setText(snd);
                    thirdQuestion.setText(test[98].strProblem);
                    fourthQuestion.setText(test[99].strProblem);
                    break;
                case 99: // user is on the last problem
                    fourthQuestion.setVisibility(View.INVISIBLE);
                    fourthAnswer.setVisibility(View.INVISIBLE);
                    fifthQuestion.setVisibility(View.INVISIBLE);
                    fifthAnswer.setVisibility(View.INVISIBLE);
                    fst = "" + test[97].attempt;
                    snd = "" + test[98].attempt;
                    firstQuestion.setText(test[97].strProblem);
                    firstAnswer.setText(fst);
                    secondQuestion.setText(test[98].strProblem);
                    secondAnswer.setText(snd);
                    thirdQuestion.setText(test[99].strProblem);
                    break;
                case 100: // user finished all 100 problems
                    // get user's time and score
                    double seconds = (System.nanoTime() - startTime) / 1000000000.0;
                    int score = 0;
                    for (int i = 0; i < 100; i++) if (test[i].attempt == test[i].answer) score++;

                    // cancel the timer since the player finished the game
                    timer.cancel();

                    // if user finished impossible mode, congratulate the user and update the impossible mode leaderboard
                    if (saveImpossible) {
                        completeImpossible = true;
                        if (timeImp.equals("empty") || seconds < parseDouble(timeImp)) {
                            timeImp = "" + String.format(Locale.getDefault(), "%.3f", seconds);
                            edit.putBoolean("legend", true);
                            edit.putString("timeImp", timeImp);
                            edit.apply();
                        }
                        Toast.makeText(this, "Congratulations! You beat the game on impossible mode in " + "" + String.format(Locale.getDefault(), "%.3f", seconds) + " seconds", Toast.LENGTH_LONG).show();
                    } else {
                        // if the user finished the fluency drill, notify the user on his or her score and time
                        Toast.makeText(this, "Score: " + "" + score + "%\tTime: " + "" + String.format(Locale.getDefault(), "%.3f", seconds) + "s", Toast.LENGTH_LONG).show();
                        if (score == 100) { // update the leaderboard if necessary
                            if (Objects.equals(first, "empty"))
                                first = "" + String.format(Locale.getDefault(), "%.3f", seconds);
                            else if (seconds < parseDouble(first)) {
                                third = second;
                                second = first;
                                first = "" + String.format(Locale.getDefault(), "%.3f", seconds);
                            } else if (Objects.equals(second, "empty"))
                                second = "" + String.format(Locale.getDefault(), "%.3f", seconds);
                            else if (seconds < parseDouble(second) && seconds >= parseDouble(first)) {
                                third = second;
                                second = "" + String.format(Locale.getDefault(), "%.3f", seconds);
                            } else if (Objects.equals(third, "empty") || seconds < parseDouble(third) && seconds >= parseDouble(second)) {
                                third = "" + String.format(Locale.getDefault(), "%.3f", seconds);
                            }
                            edit.putString("firstP", first);
                            edit.putString("secondP", second);
                            edit.putString("thirdP", third);
                            edit.apply();
                            // if user got a perfect score in less than 60s, grant user access to impossible mode
                            if (seconds < 60) {
                                impossibleAccess = true;
                                edit.putBoolean("<60", true);
                                edit.apply();
                            }
                        }
                    }
                    change();
                    break;
                default: // user is on any other problem
                    firstQuestion.setVisibility(View.VISIBLE);
                    firstAnswer.setVisibility(View.VISIBLE);
                    fifthQuestion.setVisibility(View.VISIBLE);
                    fifthAnswer.setVisibility(View.VISIBLE);
                    fst = "" + test[num - 2].attempt;
                    snd = "" + test[num - 1].attempt;
                    firstQuestion.setText(test[num - 2].strProblem);
                    firstAnswer.setText(fst);
                    secondQuestion.setText(test[num - 1].strProblem);
                    secondAnswer.setText(snd);
                    thirdQuestion.setText(test[num].strProblem);
                    fourthQuestion.setText(test[num + 1].strProblem);
                    fifthQuestion.setText(test[num + 2].strProblem);
                    break;
            }
        }
    }
}