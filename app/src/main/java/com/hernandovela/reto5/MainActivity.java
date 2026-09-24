package com.hernandovela.reto5;

import android.app.Activity;
import android.os.*;
import android.media.MediaPlayer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

public final class MainActivity extends Activity {
    private final TicTacToeGame game = new TicTacToeGame();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private BoardView board;
    private TextView status, detail, score;
    private MediaPlayer humanSound, computerSound;
    private boolean computerTurn, soundEnabled = true;
    private int wins, draws, losses;
    private final Runnable computerMove = () -> {
        if (!computerTurn || game.result()!=TicTacToeGame.PLAYING) return;
        int position = game.getComputerMove();
        if (game.setMove(TicTacToeGame.COMPUTER_PLAYER, position)) play(computerSound);
        computerTurn=false;
        finishMove();
    };
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private TextView text(String value, int size, int color) {
        TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.CENTER); v.setPadding(0,dp(3),0,dp(3)); return v;
    }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable b=new GradientDrawable(); b.setColor(color); b.setCornerRadius(dp(radius)); return b;
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        soundEnabled=getPreferences(MODE_PRIVATE).getBoolean("sound",true);
        wins=getPreferences(MODE_PRIVATE).getInt("wins",0);
        draws=getPreferences(MODE_PRIVATE).getInt("draws",0);
        losses=getPreferences(MODE_PRIVATE).getInt("losses",0);
        if(state!=null) { game.restore(state.getString("board")); computerTurn=state.getBoolean("turn"); }
        int ink=Color.rgb(26,48,46), teal=Color.rgb(20,125,115);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout outer=new LinearLayout(this); outer.setOrientation(LinearLayout.VERTICAL); outer.setGravity(Gravity.CENTER);
        scroll.addView(outer);
        outer.setPadding(dp(24),dp(8),dp(24),dp(8));
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            } else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        LinearLayout column=new LinearLayout(this); column.setOrientation(LinearLayout.VERTICAL); column.setGravity(Gravity.CENTER_HORIZONTAL);
        int available=getResources().getDisplayMetrics().widthPixels-dp(48);
        outer.addView(column,new LinearLayout.LayoutParams(Math.min(available,dp(420)),LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView eyebrow=text("RETO 05  /  GRÁFICOS Y SONIDOS",11,teal); eyebrow.setLetterSpacing(.15f); column.addView(eyebrow);
        TextView title=text("Tres en raya",28,ink); title.setTypeface(null,Typeface.BOLD); column.addView(title);
        column.addView(text("Una línea. Tres fichas. Tu próxima jugada.",14,Color.rgb(100,116,113)));
        score=text("",14,ink); score.setPadding(dp(8),dp(10),dp(8),dp(10)); score.setBackground(background(Color.rgb(229,237,233),16));
        LinearLayout.LayoutParams scoreParams=new LinearLayout.LayoutParams(-1,-2); scoreParams.setMargins(0,dp(8),0,dp(8)); column.addView(score,scoreParams);
        status=text("",21,ink); status.setTypeface(null,Typeface.BOLD); status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); column.addView(status);
        detail=text("",14,Color.rgb(100,116,113)); column.addView(detail);
        board=new BoardView(this); board.setGame(game); board.setMoveListener(this::humanMove);
        int boardSize=Math.min(available, dp(Math.max(180,Math.min(340,getResources().getConfiguration().screenHeightDp-470))));
        LinearLayout.LayoutParams boardParams=new LinearLayout.LayoutParams(boardSize,boardSize); boardParams.setMargins(0,dp(8),0,dp(8)); column.addView(board,boardParams);
        column.addView(text("TÚ  ×                         ANDROID  ○",13,teal));
        Button restart=new Button(this); restart.setText("Nueva partida"); restart.setTextColor(Color.WHITE); restart.setAllCaps(false); restart.setTextSize(16);
        restart.setBackground(background(teal,16)); restart.setOnClickListener(v->newGame());
        LinearLayout.LayoutParams buttonParams=new LinearLayout.LayoutParams(-1,dp(48)); buttonParams.setMargins(0,dp(8),0,dp(4)); column.addView(restart,buttonParams);
        Switch sound=new Switch(this); sound.setText("Efectos de sonido  "); sound.setTextSize(14); sound.setTextColor(ink); sound.setChecked(soundEnabled); sound.setMinHeight(dp(48));
        sound.setOnCheckedChangeListener((button,checked)->{
            soundEnabled=checked; getPreferences(MODE_PRIVATE).edit().putBoolean("sound",checked).apply();
            if(!checked) { if(humanSound!=null && humanSound.isPlaying()) humanSound.pause(); if(computerSound!=null && computerSound.isPlaying()) computerSound.pause(); }
        }); column.addView(sound);
        column.addView(text("Toca una casilla vacía y conecta tres fichas.\nAndroid responde después de un segundo.",12,Color.rgb(100,116,113)));
        setContentView(scroll); scroll.requestApplyInsets(); render();
    }
    private void humanMove(int position) {
        if(computerTurn || game.result()!=TicTacToeGame.PLAYING || !game.setMove('X',position)) return;
        play(humanSound);
        if(game.result()==TicTacToeGame.PLAYING) { computerTurn=true; handler.postDelayed(computerMove,1000); }
        finishMove();
    }
    private void finishMove() {
        int result=game.result();
        if(result!=TicTacToeGame.PLAYING) {
            computerTurn=false;
            if(result==TicTacToeGame.HUMAN_WINS) wins++;
            else if(result==TicTacToeGame.COMPUTER_WINS) losses++;
            else draws++;
            getPreferences(MODE_PRIVATE).edit().putInt("wins",wins).putInt("draws",draws).putInt("losses",losses).apply();
        }
        render();
    }
    private void render() {
        int result=game.result();
        score.setText("Tú  "+wins+"     ·     Empates  "+draws+"     ·     Android  "+losses);
        if(result==TicTacToeGame.HUMAN_WINS) { status.setText("¡Ganaste!"); detail.setText("Tres en línea. ¡Bien jugado!"); }
        else if(result==TicTacToeGame.COMPUTER_WINS) { status.setText("Android gana"); detail.setText("Otra partida, otra oportunidad."); }
        else if(result==TicTacToeGame.DRAW) { status.setText("¡Empate!"); detail.setText("Tablero completo. ¿Una revancha?"); }
        else { status.setText(computerTurn ? "Android está pensando…" : "Tu turno"); detail.setText(computerTurn ? "Preparando su próxima jugada" : "Juegas con X · Elige una casilla"); }
        board.setEnabled(!computerTurn && result==TicTacToeGame.PLAYING); board.invalidate();
    }
    private void newGame() { handler.removeCallbacks(computerMove); game.clearBoard(); computerTurn=false; render(); }
    private void play(MediaPlayer player) { if(soundEnabled && player!=null) { player.seekTo(0); player.start(); } }
    @Override protected void onResume() {
        super.onResume();
        humanSound=MediaPlayer.create(this,R.raw.human_move);
        computerSound=MediaPlayer.create(this,R.raw.computer_move);
        if(computerTurn && game.result()==TicTacToeGame.PLAYING) { handler.removeCallbacks(computerMove); handler.postDelayed(computerMove,1000); }
    }
    @Override protected void onPause() {
        handler.removeCallbacks(computerMove);
        if(humanSound!=null) { humanSound.release(); humanSound=null; }
        if(computerSound!=null) { computerSound.release(); computerSound=null; }
        super.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle out) { out.putString("board",game.save()); out.putBoolean("turn",computerTurn); super.onSaveInstanceState(out); }
}
