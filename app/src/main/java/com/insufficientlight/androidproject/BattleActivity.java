package com.insufficientlight.androidproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class BattleActivity extends GameActivity
{
    private static Battle battle = null;
    public static TextView Title;
    public TextView Army1;
    public TextView Army2;
    public TextView Terrain;
    public Button playerchoose;
    public TextView commandView;
    public  Button readyButton;
    public  Button retreatButton;

    public static final String TAG = "DATAPASSING";
    public String player = "player1";
    MultiplayerData multiplayerData = new MultiplayerData(); // The object that will hold important information pertaining to multi-player functionality
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final String  userID = user.getUid();//Sets the userId to the Uid(unique ID) provided by firebase for the signed in user


    public String p1t;
    public String p1a;
    public String p1c;

    private Button Bat;
    private static int count;

    public String p2t;
    public String p2a;
    public String p2c;

    public long defendInf;
    public long defendArch;
    public long defendCav;
    public long defendSiege;

    public long attackInf;
    public long attackArch;
    public long attackCav;
    public long attackSeige;
    public int batRan;

    public static void setBattle (Battle yeet)
    {
        battle = yeet;
    }
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        // sets defaults, will probably be changed in the future as more complexity happens.


        //Shows progress dialog while important tasks are completed. Will be dismissed after player data is set
        final ProgressDialog pDialog = ProgressDialog.show(BattleActivity.this,
                "Please Wait",
                "Loading...",
                true);



        //Batrun is a control variable that ensures the combat engine only runs once per cycle
        batRan = 0;


        // creates the builder that will be used for alert dialogs
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        //Clears out the commands document prior the rest of the code running
        Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(), "attacker1","defender1", "not","not");


        //Number pickers used for strategy
        NumberPicker Formation = (NumberPicker) findViewById(R.id.form);
        NumberPicker CavalryTactics = (NumberPicker) findViewById(R.id.cav);

        Title = findViewById(R.id.titleView);
        Army1 = findViewById(R.id.attackerView);
        Army2 = findViewById(R.id.defenderView);
        Terrain = findViewById(R.id.terrainView);
        Bat = findViewById(R.id.button2);
        retreatButton = findViewById(R.id.button3);

        //Set up for the tactics spinners
        final String[] Formations = {"Shield Wall","Phalanx", "Turtle Formation"};
        final String[] CavTac = {"Charge Front Lines","Flanking Operation", "Hold Cavalry"};
        Formation.setMinValue(0);
        CavalryTactics.setMinValue(0);
        Formation.setMaxValue(2);
        CavalryTactics.setMaxValue(2);
        Formation.setDisplayedValues(Formations);
        CavalryTactics.setDisplayedValues(CavTac);
        Formation.setWrapSelectorWheel(true);
        CavalryTactics.setWrapSelectorWheel(true);


        //Displays the battle information for both sides, the terrain, and the name of the area they're fighting in.
        Terrain.setText(battle.getTerrain().getTerrainType());
        Title.setText("The battle of "+ battle.getLocation()+"!");
        Army1.setText("Attacker: " + battle.attacker.armyName);
        Army2.setText("Defender: " + battle.defender.armyName);
        Army1.append("\n Infantry: " + battle.attacker.numInf + "\n Archers: " + battle.attacker.numArc + "\n Cavalry :" + battle.attacker.numCav +"\n Siege Weapons: " + battle.attacker.numSie);
        Army2.append("\n Infantry: " + battle.defender.numInf + "\n Archers: " + battle.defender.numArc + "\n Cavalry :" + battle.defender.numCav +"\n Siege Weapons: " + battle.defender.numSie);




        /** **There is a duplicate of this code in a method setSides. This allows it to be rerun when needed but avoids variable errors in the anonymous inner classes**
         * The following code starts a set of tasks that accomplish the following:
         * -Determine if the userIDs in the data abse are new or old,
         * -Determine if there is another user already set
         * -If a user is there it sets the current user as the opposite
         * -If there is none it picks randomly between attacker/defender and uploads the data
         */
        //See the seperate method for line by line commenting
        multiplayerData.getmUserIdReferance().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("playCheck")) {
                        if (documentSnapshot.get("playCheck").equals("true")) {
                            if (Math.random() < 0.5) {
                                Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "attacker", "playCheck", userID, "false");
                                player = "attacker1";
                                Title.append(" \n You are the ATTACKER");
                            } else {
                                Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "defender", "playCheck", userID, "false");
                                player = "defender1";
                                Title.append(" \n You are the DEFENDER");
                            }
                        }
                        else {
                            if (documentSnapshot.contains("attacker") && !documentSnapshot.contains("defender")) {
                                Multiplayer_Logic.setThreeData(multiplayerData.mUserIdReferance, "attacker", "defender", "playCheck", documentSnapshot.getString("attacker"), userID, "false");
                                player = "defender1";
                                Title.append("\n You are the DEFENDER");
                            } else if (documentSnapshot.contains("defender") && !documentSnapshot.contains("attacker")) {
                                Multiplayer_Logic.setThreeData(multiplayerData.mUserIdReferance, "attacker", "defender","playCheck", userID, documentSnapshot.getString("defender"),"false");
                                player = "attacker1";
                                Title.append(" \n You are the ATTACKER");
                            } else {
                                if (Math.random() < 0.5) {
                                    Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "attacker", "playCheck", userID, "false");
                                    player = "attacker1";
                                    Title.append(" \n You are the ATTACKER");
                                } else {
                                    Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "defender", "playCheck", userID, "false");
                                    player = "defender1";
                                    Title.append(" \n You are the DEFENDER");
                                }
                            }
                        }
                    }
                    else
                    {
                        if (Math.random() < 0.5) {
                            Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "attacker", "playCheck", userID, "false");
                            player = "attacker1";
                            Title.append(" \n You are the ATTACKER");
                        } else {
                            Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "defender", "playCheck", userID, "false");
                            player = "defender1";
                            Title.append(" \n You are the DEFENDER");
                        }
                    }
                }
                else
                {
                    Multiplayer_Logic.setSingleData(multiplayerData.getmUserIdReferance(),"temp","temp");
                    setSides();
                }
                cancelLoadDialog(pDialog);
            }
        });



        //Listeners for the tactics spinners
        Formation.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                p1t = Formations[newVal];
            }
        });

        CavalryTactics.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                p1c = CavTac[newVal];
            }
        });

        //This button allows the user to retreat by sending the retreat command
        retreatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.equals("attacker1"))
                {
                    Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(),"attacker1","defender1","retreat","temp");
                }
                if (player.equals("defender1"))
                {
                    Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(),"attacker1","defender1","temp","retreat");
                }
            }
        });



        /**
         * When the ready button is clicked this method will run. It handles setting commands on the database and running them if possible.
         */
        Bat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {Log.i("testy", player);
                //Handles setting the commands in the database.
                //Starts by pulling down the current copy of the document
                multiplayerData.getCommandDecisionKey().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        batRan = 0;
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) //Gets the data and reifies it exists
                            {
                                if (player.equals("attacker1"))//Breaks down actions depending on the player taking them, both are identical
                                {
                                    Log.i("testy", player);
                                    if (document.getData().get("defender1").equals("ready")) //if the other player has already hit their button
                                    {
                                        runBat("attacker",pDialog); // runs the combat mechanics for the attacker
                                    }
                                    else
                                    {Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(),"attacker1","defender1","ready","not");} // changes player1's status command to ready
                                }
                                if (player.equals("defender1"))
                                {Log.i("testy", player);
                                    if (document.getData().get("attacker1").equals("ready"))//if the other player has already hit their button it sets both to ready. allowing the attacker to run their cooodesss
                                    {
                                        Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(),"attacker1","defender1","ready","ready");
                                        //runBat();
                                    }
                                    else
                                    {Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(),"attacker1","defender1","not","ready");}// changes player2's status command to ready
                                }
                            }
                        }
                    }
                });
            }
        });



        /**
         * Monitors the battle command document for any changes, if both are set to ready it runs battle loop for the attacker only.
         * If a user elects to retreat it takes the command and shows a dialog.
         */
        multiplayerData.getCommandDecisionKey().addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
            {
                if (documentSnapshot.exists())
                {
                    if (documentSnapshot.contains("attacker1") && documentSnapshot.contains("defender1") && documentSnapshot.getString("defender1") !=null && documentSnapshot.getString("attacker1")!=null)
                    {
                        if (documentSnapshot.getString("attacker1").equals("ready") && documentSnapshot.getString("defender1").equals("ready") && player.equals("attacker1")) {
                            runBat("attacker", pDialog);
                        }
                        if (documentSnapshot.getString("attacker1").equals("retreat"))
                        {
                            builder.setMessage("The attacker has retreated.").setCancelable(false).setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setTitle("Attacker Retreat");
                            alert.show();
                        }
                        if (documentSnapshot.getString("defender1").equals("retreat"))
                        {
                            builder.setMessage("The defender has retreated.").setCancelable(false).setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    finish();
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setTitle("Defender Retreat");
                            alert.show();
                        }
                    }
                }
            }
        });


        /**
         * When the attacker changes the defender loss data in the database this method is called, it then sets the local variables for the losses and runs the battle loop for the defender, creating the popup
         */
        multiplayerData.getDefendLossesReferance().addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e)
            {
                if (documentSnapshot.exists())
                {
                    Log.i("data10101", "before " + player + " " + documentSnapshot.getString("defenderID"));
                    if (documentSnapshot.getString("defenderID") != null && documentSnapshot.getString("defenderID").equals(player))
                    {
                        pDialog.show();
                        Log.i("data10101", "working");
                        defendInf = (long) documentSnapshot.getData().get("inf");
                        defendArch =(long) documentSnapshot.getData().get("arch");
                        defendCav = (long)documentSnapshot.getData().get("cav");
                        defendSiege = (long)documentSnapshot.getData().get("siege");

                        //This retrieves the attacker army size from the database to be displayed on screen. Once it's done it runs the method runBat()
                        multiplayerData.getAttackerLossesReferance().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists())
                                {
                                    attackInf = (long) documentSnapshot.getData().get("inf");
                                    attackArch =(long) documentSnapshot.getData().get("arch");
                                    attackCav = (long)documentSnapshot.getData().get("cav");
                                    attackSeige = (long)documentSnapshot.getData().get("siege");
                                    runBat("defender", pDialog);
                                }
                            }
                        });
                    }
                }
            }
        });


    }

    public void setSides()
    {
        /**
         * The following button starts a set of tasks that accomplish the following:
         * -Determine if the userIDs in the data abse are new or old,
         * -Determine if there is another user already set
         * -If a user is there it sets the current user as the opposite
         * -If there is none it picks randomly between attacker/defender and uploads the data
         */
        multiplayerData.getmUserIdReferance().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    //Play check acts to ensure the battle isn't aon old one. If it's true a new battle is overwriten
                    if (documentSnapshot.contains("playCheck")) {
                        if (documentSnapshot.get("playCheck").equals("true")) {
                            //Randomly picks whether the user is the attacker or defender
                            if (Math.random() < 0.5) {
                                Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "attacker", "playCheck", userID, "false");
                                player = "attacker1";
                                Title.append(" \n You are the ATTACKER");
                            } else {
                                Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "defender", "playCheck", userID, "false");
                                player = "defender1";
                                Title.append(" \n You are the DEFENDER");
                            }
                        }
                        else {
                            //If there is already an attacker make this user a defender
                            if (documentSnapshot.contains("attacker") && !documentSnapshot.contains("defender")) {
                                Multiplayer_Logic.setThreeData(multiplayerData.mUserIdReferance, "attacker", "defender", "playCheck", documentSnapshot.getString("attacker"), userID, "false");
                                player = "defender1";
                                Title.append("\n You are the DEFENDER");
                                //If there is already a defender make this user an attacker
                            } else if (documentSnapshot.contains("defender") && !documentSnapshot.contains("attacker")) {
                                Multiplayer_Logic.setThreeData(multiplayerData.mUserIdReferance, "attacker", "defender","playCheck", userID, documentSnapshot.getString("defender"),"false");
                                player = "attacker1";
                                Title.append(" \n You are the ATTACKER");
                            } else {
                                //Randomly pick if there is no other user yet
                                if (Math.random() < 0.5) {
                                    Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "attacker", "playCheck", userID, "false");
                                    player = "attacker1";
                                    Title.append(" \n You are the ATTACKER");
                                } else {
                                    Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "defender", "playCheck", userID, "false");
                                    player = "defender1";
                                    Title.append(" \n You are the DEFENDER");
                                }
                            }
                        }
                    }
                    else
                    {
                        if (Math.random() < 0.5) {
                            Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "attacker", "playCheck", userID, "false");
                            player = "attacker1";
                            Title.append(" \n You are the ATTACKER");
                        } else {
                            Multiplayer_Logic.setTwoData(multiplayerData.mUserIdReferance, "defender", "playCheck", userID, "false");
                            player = "defender1";
                            Title.append(" \n You are the DEFENDER");
                        }
                    }
                }
                else
                {
                    //If the UserId document doesn't exist yet this creates it with fillers and reruns the code to set the defender/attacker. This prevents nullPointerExceptions
                    Multiplayer_Logic.setSingleData(multiplayerData.getmUserIdReferance(),"temp","temp");
                    setSides();
                }

            }
        });

    }


    /**
     * RunBat is the method called to run the combat engine when both players are ready. In doing so it completes the following tasks
     * -Determines if it is the defender or attacker running the method.
     *
     * If it's the attacker it will run combatEngine, update the troop numbers on screen, show an alertDialog with troop losses, and uploads the new troop counts to the database for the defender to pull.
     *
     * If it's the defender it will  be triggered by the database being updated.
     * It then updates the troop counts with the pulled data, and displays an alertDialog with the troop losses.
     *
     */
    public void runBat(String side, ProgressDialog progressDialog)
    {
        //Pulls down the user data to allow it to be reloaded with a change
        multiplayerData.getmUserIdReferance().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //Sets placeCheck to true indicating that the battle has been used
                Multiplayer_Logic.setThreeData(multiplayerData.mUserIdReferance, "attacker", "defender", "playCheck", documentSnapshot.getString("attacker"), documentSnapshot.getString("defender"), "true");
            }
        });
        //This allows the method to run only once per hit of the ready button
        if (batRan == 0) {
            batRan = 1;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String displayString = "";

            if (side.equals("attacker"))
            {
                Log.i("Helen, help lol", "onClick: don't die keed");
                p2t = "Shield Wall";
                p2c = "Charge Front Lines";

                //sets the attackers troop count prior to combat
                int infAtk = battle.getAttacker().getNumInf();
                int archAtk = battle.getAttacker().getNumArc();
                int cavAtk = battle.getAttacker().getNumCav();
                int sieAtk = battle.getAttacker().getNumSie();

                //runs the combat
                StandardSkirmish skirmish = new StandardSkirmish(count, battle.attacker.playerTag, battle.defender.playerTag, battle,p1t, p1c, p2t, p2c);
                CombatEngine.calculateLosses(skirmish);

                count = count + 1;
                //Updates the screen's troop count
                Army1.setText("Attacker: " + battle.attacker.armyName);
                Army2.setText("Defender: " + battle.defender.armyName);
                Army1.append("\n Infantry: " + battle.getAttacker().getNumInf() + "\n Archers: " + battle.getAttacker().getNumArc() + "\n Cavalry :" + battle.getAttacker().getNumCav() +"\n Siege Weapons: " + battle.getAttacker().getNumSie());
                Army2.append("\n Infantry: " + battle.getDefender().getNumInf() + "\n Archers: " + battle.getDefender().getNumArc() + "\n Cavalry :" + battle.getDefender().getNumCav() +"\n Siege Weapons: " + battle.getDefender().getNumSie());


                Log.i("Sheed", "Noooo Halp");
                //Sets the losses of the attacker to be displayed
                int infAtkLoss =infAtk- battle.getAttacker().getNumInf();
                int archAtkLoss = archAtk - battle.getAttacker().getNumArc();
                int cavAtkLoss = cavAtk -battle.getAttacker().getNumCav();
                int sieAtkLoss = sieAtk -battle.getAttacker().getNumSie();


                //The following lines of code create the atert dialog that show the total troop losses for each player
                //In the future player IDs will in some form be pulled from the battle object or the like.
                // Builds the content of the dialog from the data of combat engine.
                displayString = "Infantry Lost: " + infAtkLoss + "\n Archers Lost: " +
                        archAtkLoss + "\n Cavalry Lost: " + cavAtkLoss +
                        "\n Seige Weapons Lost: " + sieAtkLoss;

                //Cancels the load dialog
                cancelLoadDialog(progressDialog);
                builder.setMessage(displayString).setCancelable(false).setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("Troops Lost In Battle");
                alert.show();
                Multiplayer_Logic.setTwoData(multiplayerData.getCommandDecisionKey(), "attacker1", "defender1", "not", "not");
                // ends building the alert dialog

                //Uploads the new numbers to the database
                Multiplayer_Logic.setSixData(multiplayerData.getAttackerLossesReferance(), "attackerID", "inf", "arch", "cav", "siege", "changeCheck","attacker1", skirmish.getBattle().getAttacker().getNumInf(), skirmish.getBattle().getAttacker().getNumArc(), skirmish.getBattle().getAttacker().getNumCav(), skirmish.getBattle().getAttacker().getNumSie(),"changed"+Math.random());
                Multiplayer_Logic.setSixData(multiplayerData.getDefendLossesReferance(), "defenderID", "inf", "arch", "cav", "siege",  "changeCheck","defender1", skirmish.getBattle().getDefender().getNumInf(), skirmish.getBattle().getDefender().getNumArc(), skirmish.getBattle().getDefender().getNumCav(), skirmish.getBattle().getDefender().getNumSie(),"changed"+Math.random());

            }

            if (side.equals("defender"))
            {
                Log.i("1010", "DEFENDERBATTLLE CALLED");
                Log.i("110101001111011", "working " + defendInf);


                //sets the defender losses
                int infLossDef = battle.getDefender().getNumInf() - (int) defendInf;
                int cavLossDef = battle.getDefender().getNumCav()-(int) defendCav;
                int archLossDef = battle.getDefender().getNumArc()-(int) defendArch;
                int sieLossDef = battle.getDefender().getNumSie()-(int) defendSiege;

                //sets the new defender troop counts
                battle.getDefender().setNumInf((int) defendInf);
                battle.getDefender().setNumCav((int) defendCav);
                battle.getDefender().setNumArc((int) defendArch);
                battle.getDefender().setNumSie((int) defendSiege);

                //set the new army size for the attacker
                battle.getAttacker().setNumInf((int) attackInf);
                battle.getAttacker().setNumCav((int) attackCav);
                battle.getAttacker().setNumArc((int) attackArch);
                battle.getAttacker().setNumSie((int) attackSeige);
                Log.i("defffffff", "defffffff tot " + battle.getDefender().getNumCav());

                //updates the player's screen
                Army1.setText("Attacker: " + battle.attacker.armyName);
                Army2.setText("Defender: " + battle.defender.armyName);
                Army2.append("\n Infantry: " + battle.getDefender().getNumInf() + "\n Archers: " + battle.getDefender().getNumArc() + "\n Cavalry :" + battle.getDefender().getNumCav() + "\n Siege Weapons: " + battle.getDefender().getNumSie());
                Army1.append("\n Infantry: " + battle.getAttacker().getNumInf() + "\n Archers: " + battle.getAttacker().getNumArc() + "\n Cavalry :" + battle.getAttacker().getNumCav() + "\n Siege Weapons: " + battle.getAttacker().getNumSie());


                //creates the string to display from losses data
                displayString = "Infantry Lost: " + infLossDef + "\n Archers Lost: " +
                        archLossDef + "\n Cavalry Lost: " + cavLossDef +
                        "\n Seige Weapons Lost: " + sieLossDef;

                //builds the alert dialog
                builder.setMessage(displayString).setCancelable(false).setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });
                //sets the title and shows it
                AlertDialog alert = builder.create();
                alert.setTitle("Troops Lost In Battle12");
                cancelLoadDialog(progressDialog);
                alert.show();

            }
        }
    }
    //Method to kill progress dialogs
    public void cancelLoadDialog(ProgressDialog dialog)
    {
        dialog.dismiss();
    }

}


