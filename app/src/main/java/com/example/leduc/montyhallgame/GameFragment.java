package com.example.leduc.montyhallgame;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.leduc.montyhallgame.MainFragment.NEW_CLICKED;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    //static string
    public static final String PREF_NAME = "MontyHall";
    public static final String Car_index = "Car_index";
    public static final String Chosen = "Chosen";
    public static final String Hint_door = "hint_door";
    public static final String Other_door = "Other_door";
    public static final String D1 = "door1";
    public static final String D2 = "door2";
    public static final String D3 = "door3";
    public static final String W = "win";
    public static final String L = "loss";

    //instance variables
    private boolean[] dict;
    private boolean[] door_stage;
    private TextView tv;
    private TextView win;
    private TextView loss;
    private TextView total;
    private Button bt;
    private View firework;
    private Timer t;
    private int chosen_index = -1;
    private int hint_door = -1;
    private int the_other_door = -1;
    private int car_index = -1;
    private int number_win = 0;
    private int number_loss = 0;
    private ArrayList<View> list_View;
    private SharedPreferences.Editor pref_ed;
    public AudioAttributes aa;
    private SoundPool soundPool;
    private int carSound, doorSound, failSound, winSound, goatSound;


    private void calculated_hint_door(int chosen_index){
        if(chosen_index == 0){
            get_hint_door(1, 2);
        }else if(chosen_index == 2){
            get_hint_door(0, 1);
        }else{
            get_hint_door(0,2);
        }
    }

    private void get_hint_door(int a, int b){
        double c = Math.random();
        if(c > 0.5){
            hint_door = a;
            the_other_door = b;
        }else{
            hint_door = b;
            the_other_door = a;
        }
    }


    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_game, container, false);

        final View door1 = root.findViewById(R.id.door1);
        View door2 = root.findViewById(R.id.door2);
        View door3 = root.findViewById(R.id.door3);

        tv = root.findViewById(R.id.prompt);
        win = root.findViewById(R.id.win);
        loss = root.findViewById(R.id.loss);
        total = root.findViewById(R.id.total);
        firework = root.findViewById(R.id.firework);
        firework.setVisibility(View.INVISIBLE);

        list_View = new ArrayList<>();
        dict = new boolean[]{false, false, false};
        door_stage = new boolean[]{false, false, false};
        list_View.add(door1);
        list_View.add(door2);
        list_View.add(door3);
        bt = root.findViewById(R.id.button);

        aa = new AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(aa)
                .build();

        carSound = soundPool.load(getContext(), R.raw.car_sound,1);
        goatSound = soundPool.load(getContext(), R.raw.goat_sound,1);
        doorSound = soundPool.load(getContext(), R.raw.door_sound_short,1);
        winSound = soundPool.load(getContext(), R.raw.win_sound,1);
        failSound = soundPool.load(getContext(), R.raw.fail_sound,1);

        return root;
    }

    private void reload_game(){
        //set the game to the beginning stage
        if(door_stage[0] && door_stage[1] && door_stage[2]){
            dict = new boolean[]{false, false, false};
            door_stage = new boolean[]{false, false, false};
            tv.setText(R.string.choose_a_door);
            for(int i = 0; i < list_View.size(); i ++) {
                ImageButton a = (ImageButton) list_View.get(i);
                a.setImageLevel(0);
                a.setClickable(true);
            }
            firework.setVisibility(View.INVISIBLE);
            car_index = (int) (Math.random() * 2);
            dict[car_index] = true;
            chosen_index = -1; hint_door = -1; the_other_door = -1;
            bt.setText(R.string.Open_door);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

        //listener for door
        for(int i = 0; i < list_View.size(); i ++){
            final int finalI = i;
            list_View.get(finalI).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //run through all the close door
                    chosen_index = finalI;
                    for (int j = 0; j < dict.length; j++){
                        ImageButton not_chose_door = (ImageButton) list_View.get(j);
                        // if the door is chosen, display the image
                        // if the user change the chosen door, reset the chosen door image
                        if(j!= chosen_index && !door_stage[j]){
                            not_chose_door.setImageLevel(0);
                        }else if(j == chosen_index){
                            not_chose_door.setImageLevel(1);
                        }
                    }
                }
            });
        }

        //Button (OPEN DOOR) listener
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stage 1: all door were closed
                if(!door_stage[0] && !door_stage[1] && !door_stage[2] && chosen_index != -1){
                    bt.setClickable(false);

                    //randomly assign which one has a car.
                    for(int i = 0; i< 3; i++){
                        dict[i] = false;
                    }
                    car_index = (int) (Math.random() * 2);
                    dict[car_index] = true;


                    //find the hint door.
                    for (int i = 0; i < list_View.size(); i++) {
                        //if chosen is a goat
                        if (!dict[chosen_index]) {
                            if (!dict[i] && i != chosen_index) {
                                hint_door = i;
                                the_other_door = car_index;
                            }
                        } else {
                            //pull a 50 - 50 to open the hind door if chosen door is a car
                            calculated_hint_door(chosen_index);
                        }
                    }

                    final ImageButton Hint_door = (ImageButton) list_View.get(hint_door);
                    final ImageButton The_Other_door = (ImageButton) list_View.get(the_other_door);
                    final ImageButton Chosen = (ImageButton) list_View.get(chosen_index);
                    Hint_door.setClickable(false);
                    The_Other_door.setClickable(false);
                    Chosen.setClickable(false);

                    //start the timer
                    t = new Timer();
                    t.schedule(new TimerTask() {
                        int count = 3;

                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(R.string.nortify_hint_door);
                                    if(count <= 6 && count >= 4){
                                        if(count == 6){
                                            soundPool.play(doorSound,1f,1f,0,0,1f);
                                        }
                                        //hint door is opening
                                        Hint_door.setImageLevel(count);
                                        The_Other_door.setImageLevel(count);
                                    }else if (count > 6){
                                        Hint_door.setImageLevel(3);
                                        //door cant click while image count down is running
                                        The_Other_door.setClickable(true);
                                        Chosen.setClickable(true);
                                        door_stage[hint_door] = true;
                                        soundPool.play(goatSound,1f,1f,0,0,1f);
                                        The_Other_door.setImageLevel(0);
                                        tv.setText(R.string.ask);
                                        t.cancel();
                                        bt.setClickable(true);
                                    }
                                    count++;
                                }
                            });

                        }
                    }, 0, 1000);
                }
                //stage 2 if the hint door is opened
                else if(chosen_index != -1 && door_stage[hint_door] && !door_stage[car_index]){
                    bt.setClickable(false);
                    //find the chosen door on stage 2
                    if(chosen_index == the_other_door){
                        for(int i = 0; i < 3; i++){
                            if(i != chosen_index && i != hint_door){
                                the_other_door = i;
                            }
                        }
                    }

                    final ImageButton The_Other_door = (ImageButton) list_View.get(the_other_door);
                    final ImageButton Car = (ImageButton) list_View.get(car_index);
                    final ImageButton Chosen_door = (ImageButton) list_View.get(chosen_index);

                    The_Other_door.setClickable(false);
                    Chosen_door.setClickable(false);

                    t = new Timer();
                    t.schedule(new TimerTask() {
                        int count = 3;
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(R.string.result);
                                    if(count <= 6 && count >= 4){
                                        The_Other_door.setImageLevel(count);
                                        if(count==6){
                                            soundPool.play(doorSound,1f,1f,0,0,1f);
                                        }
                                    }else if (count > 6){
                                        door_stage[car_index] = true;
                                        Car.setImageLevel(2);
                                        bt.setClickable(true);
                                        bt.setText(R.string.restart);
                                        door_stage[the_other_door] = true;
                                        door_stage[chosen_index] = true;
                                        if(car_index == chosen_index){
                                            //win
                                            tv.setText(R.string.congrat);
                                            The_Other_door.setImageLevel(0);
                                            firework.setVisibility(View.VISIBLE);
                                            win.setText(String.valueOf(number_win+=1));
                                            soundPool.play(carSound,1f,1f,0,0,1f);
                                            total.setText(String.valueOf(number_win + number_loss));
                                        }else{
                                            //lost
                                            tv.setText(R.string.lost);
                                            Chosen_door.setImageLevel(1);
                                            loss.setText(String.valueOf(number_loss+=1));
                                            soundPool.play(failSound,1f,1f,0,0,1f);
                                            soundPool.play(winSound,1f,1f,1,20,1f);
                                            total.setText(String.valueOf(number_win + number_loss));
                                        }
                                        t.cancel();
                                    }
                                    count++;
                                }
                            });

                        }
                    }, 0, 1000);
                }else if (door_stage[0] && door_stage[1] && door_stage[2]){
                    //Game now restart
                    reload_game();
                }
                //small animation when button change text
                RotateAnimation ranim = (RotateAnimation)AnimationUtils.loadAnimation(GameFragment.this.getContext(), R.anim.animation);
                ranim.setFillAfter(true);
                bt.setAnimation(ranim);
            }
        });


        pref_ed = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        boolean newclick = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean("NewClicked", false);

        //if it is a new game
        if(newclick){
            number_win = 0;
            number_loss = 0;
            reload_game();
        }else{
            load_stage();
        }

        win.setText(String.valueOf(number_win));
        loss.setText(String.valueOf(number_loss));
        total.setText(String.valueOf(number_win + number_loss));
    }

    @Override
    public void onStop() {
        super.onStop();
        if(t!= null){
            t.cancel();
        }
        pref_ed.putBoolean(NEW_CLICKED, false).apply();
        pref_ed.putInt(W, number_win).apply();
        pref_ed.putInt(L, number_loss).apply();
        pref_ed.putInt(Car_index, car_index).apply();
        pref_ed.putInt(Chosen, chosen_index).apply();
        pref_ed.putInt(Hint_door, hint_door).apply();
        pref_ed.putInt(Other_door, the_other_door).apply();
        pref_ed.putBoolean(D1, door_stage[0]).apply();
        pref_ed.putBoolean(D2, door_stage[1]).apply();
        pref_ed.putBoolean(D3, door_stage[2]).apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void load_stage(){
        //rebuild the stage after continue game
        car_index = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(Car_index, -1);
        chosen_index = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(Chosen, -1);
        hint_door = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(Hint_door, -1);
        the_other_door = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(Other_door, -1);
        door_stage[0] = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(D1, false);
        door_stage[1] = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(D2, false);
        door_stage[2] = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(D3, false);
        number_win = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(W, 0);
        number_loss = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(L, 0);

        //stage 1 no door is yet open, may be a door is chosen
        if(chosen_index == -1){
            reload_game();
        }else {
            ImageButton Chosen_door = (ImageButton) list_View.get(chosen_index);
            Chosen_door.setImageLevel(1);
            if(hint_door != -1 && door_stage[hint_door]) {
                ImageButton The_Other_door = (ImageButton) list_View.get(the_other_door);
                ImageButton Hint_door = (ImageButton) list_View.get(hint_door);
                ImageButton Car = (ImageButton) list_View.get(car_index);
                Hint_door.setImageLevel(3);
                door_stage[hint_door] = true;
                Hint_door.setClickable(false);
                if (!door_stage[car_index]) {
                    //we at stage 2:
                    The_Other_door.setImageLevel(0);
                    tv.setText(R.string.ask);
                } else {
                    Chosen_door.setClickable(false);
                    The_Other_door.setClickable(false);
                    //all door has reveal
                    Car.setImageLevel(2);
                    bt.setText(R.string.restart);
                    if (car_index == chosen_index) {
                        tv.setText(R.string.congrat);
                        The_Other_door.setImageLevel(0);
                        firework.setVisibility(View.VISIBLE);
                    } else {
                        tv.setText(R.string.loss);
                        Chosen_door.setImageLevel(1);
                    }
                }
            }else if(hint_door != -1 && !door_stage[hint_door]){
                //handle quit while image count down at stage 1
                for(int i = 0; i < 3; i++) {
                    if(i != chosen_index) {
                        ImageButton a = (ImageButton) list_View.get(i);
                        a.setImageLevel(0);
                    }
                    tv.setText(R.string.choose_a_door);
                }
            }
        }
    }
}