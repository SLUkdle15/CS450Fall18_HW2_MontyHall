package com.example.leduc.montyhallgame;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    //TODO static final all string
    public static final String PREF_NAME = "MontyHall";
    private boolean[] dict;
    private boolean[] door_stage;
    private TextView tv;
    private TextView win;
    private TextView loss;
    private Button bt;
    private Animator anim;
    private Timer t;
    private int chosen_index = -1;
    private int hint_door = -1;
    private int the_other_door = -1;
    private int car_index = -1;
    private int number_win = 0;
    private int number_loss = 0;
    private ArrayList<View> list_View;
    private SharedPreferences.Editor pref_ed;

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

        list_View = new ArrayList<>();
        dict = new boolean[]{false, false, false};
        door_stage = new boolean[]{false, false, false};
        list_View.add(door1);
        list_View.add(door2);
        list_View.add(door3);
        bt = root.findViewById(R.id.button);

        return root;
    }

    private void reload_game(){
        if(door_stage[0] && door_stage[1] && door_stage[2]){
            dict = new boolean[]{false, false, false};
            door_stage = new boolean[]{false, false, false};
            tv.setText(R.string.choose_a_door);
            for(int i = 0; i < list_View.size(); i ++) {
                ImageButton a = (ImageButton) list_View.get(i);
                a.setImageLevel(0);
                a.setClickable(true);
            }
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

        //Button listener
        //TODO disable button while image count
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RotateAnimation ranim = (RotateAnimation)AnimationUtils.loadAnimation(bt.getContext(), R.anim.animation);
                ranim.setFillAfter(true); //For the textview to remain at the same place after the rotation
                bt.setAnimation(ranim);

                // stage 1: all door were closed
                if(!door_stage[0] && !door_stage[1] && !door_stage[2] && chosen_index != -1){
                    bt.setClickable(false);

                    //randomly assign which one has a car.
                    //if(car_index == -1) {
                    for(int i = 0; i< 3; i++){
                        dict[i] = false;
                    }
                        car_index = (int) (Math.random() * 2);
                        dict[car_index] = true;
                    //}

                    //find the hint door.
                    //if(hint_door == -1) {
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
                                        Hint_door.setImageLevel(count);
                                        The_Other_door.setImageLevel(count);
                                    }else if (count > 6){
                                        Hint_door.setImageLevel(3);
                                        //door cant click while image count down is running
                                        The_Other_door.setClickable(true);
                                        Chosen.setClickable(true);
                                        door_stage[hint_door] = true;
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
                    //now what is the chosen door
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
                                    }else if (count > 6){
                                        door_stage[car_index] = true;
                                        Car.setImageLevel(2);
                                        bt.setClickable(true);
                                        bt.setText(R.string.restart);
                                        door_stage[the_other_door] = true;
                                        door_stage[chosen_index] = true;
                                        if(car_index == chosen_index){
                                            tv.setText(R.string.congrat);
                                            The_Other_door.setImageLevel(0);
                                            win.setText(String.valueOf(number_win+=1));
                                        }else{
                                            tv.setText(R.string.lost);
                                            Chosen_door.setImageLevel(1);
                                            loss.setText(String.valueOf(number_loss+=1));
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if(t!= null){
            t.cancel();
        }
        pref_ed.putBoolean("NewClicked", false).apply();
        pref_ed.putBoolean("CONClicked", false).apply();
        pref_ed.putInt("win", number_win).apply();
        pref_ed.putInt("loss", number_loss).apply();
        pref_ed.putInt("Car_index", car_index).apply();
        pref_ed.putInt("Chosen", chosen_index).apply();
        pref_ed.putInt("hint_door", hint_door).apply();
        pref_ed.putInt("Other_door", the_other_door).apply();
        pref_ed.putBoolean("door1", door_stage[0]).apply();
        pref_ed.putBoolean("door2", door_stage[1]).apply();
        pref_ed.putBoolean("door3", door_stage[2]).apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void load_stage(){
        car_index = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("Car_index", -1);
        chosen_index = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("Chosen", -1);
        hint_door = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("hint_door", -1);
        the_other_door = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("Other_door", -1);
        door_stage[0] = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean("door1", false);
        door_stage[1] = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean("door2", false);
        door_stage[2] = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean("door3", false);
        number_win = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("win", 0);
        number_loss = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt("loss", 0);

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
                    } else {
                        tv.setText(R.string.loss);
                        Chosen_door.setImageLevel(1);
                    }
                }
            }else if(hint_door != -1 && !door_stage[hint_door]){
                //handle quit while image cout down at stage 1
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