package com.example.leduc.montyhallgame;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment{
    private boolean[] dict;
    private boolean[] door_stage;
    private static TextView tv;
    private Timer t;
    private int chosen_index = -1;
    private int hint_door = -1;
    private int the_other_door = -1;
    private int car_index;

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

        View door1 = root.findViewById(R.id.door1);
        View door2 = root.findViewById(R.id.door2);
        View door3 = root.findViewById(R.id.door3);

        tv = root.findViewById(R.id.prompt);

        final ArrayList<View> list_View = new ArrayList<>();
        dict = new boolean[]{false, false, false};
        door_stage = new boolean[]{false, false, false};
        list_View.add(door1);
        list_View.add(door2);
        list_View.add(door3);

        //randomly assign which one has a car.
        car_index = (int) (Math.random() * 2);
        dict[car_index] = true;


        //TODO load the stage here after reconstructing

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
        //TODO disable image clickable while countdown
        Button bt = root.findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stage 1: all door were closed
                if(!door_stage[0] && !door_stage[1] && !door_stage[2] && chosen_index != -1){
                    //find the hint door.
                    for(int i = 0; i < list_View.size(); i++){
                        //if chosen is a goat
                        if (!dict[chosen_index]){
                            if(!dict[i] && i != chosen_index){
                                hint_door = i;
                                the_other_door = car_index;
                            }
                        }else{
                            //pull a 50 - 50 to open the hind door if chosen door is a car
                            calculated_hint_door(chosen_index);
                        }
                    }

                    //start the timer
                    t = new Timer();
                    t.schedule(new TimerTask() {
                        int count = 3;

                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                ImageButton Hint_door = (ImageButton) list_View.get(hint_door);
                                ImageButton The_Other_door = (ImageButton) list_View.get(the_other_door);
                                @Override
                                public void run() {
                                    tv.setText(R.string.nortify_hint_door);
                                    if(count <= 6 && count >= 4){
                                        Hint_door.setImageLevel(count);
                                        The_Other_door.setImageLevel(count);
                                    }else if (count > 6){
                                        Hint_door.setImageLevel(3);
                                        Hint_door.setClickable(false);
                                        door_stage[hint_door] = true;
                                        The_Other_door.setImageLevel(0);
                                        tv.setText(R.string.ask);
                                        t.cancel();
                                    }
                                    count++;
                                }
                            });

                        }
                    }, 0, 1000);
                }
                //stage 2 if the hint door is opened
                else if(door_stage[0] || door_stage[1] || door_stage[2]){
                    //now what is the chosen door
                    if(chosen_index == the_other_door){
                        for(int i = 0; i < 3; i++){
                            if(i != chosen_index && i != hint_door){
                                the_other_door = i;
                            }
                        }
                    }

                    t = new Timer();
                    t.schedule(new TimerTask() {
                        int count = 3;
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                ImageButton The_Other_door = (ImageButton) list_View.get(the_other_door);
                                ImageButton Car = (ImageButton) list_View.get(car_index);
                                ImageButton Chosen_door = (ImageButton) list_View.get(chosen_index);
                                @Override
                                public void run() {
                                    tv.setText(R.string.result);
                                    if(count <= 6 && count >= 4){
                                        The_Other_door.setImageLevel(count);
                                    }else if (count > 6){
                                        door_stage[car_index] = true;
                                        Car.setClickable(false);
                                        Car.setImageLevel(2);
                                        if(car_index == chosen_index){
                                            The_Other_door.setImageLevel(0);
                                        }else{
                                            Chosen_door.setImageLevel(1);
                                        }
                                        t.cancel();
                                    }
                                    count++;
                                }
                            });

                        }
                    }, 0, 1000);
                }
            }
        });


        return root;
    }

}