/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import java.awt.GridLayout;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.text.*;

public class Controller{
    
    public static double time_ms=0;
    private static double counter=1, cur_avg=0;
    
    static final JFrame myFrame=new JFrame("Sensor data window");
    static JLabel l1=new JLabel("*******************WAITING FOR DATA*******************");
    static JLabel l2=new JLabel("Loading...",JLabel.CENTER);
    static JLabel l3=new JLabel("Loading...",JLabel.CENTER);
    static JLabel l4=new JLabel("Loading...",JLabel.CENTER);


    public static void dataFix()
    {
        Library.stayCool(1000);
        /*reading sensor inputs and saving it in a string*/
        String save="";
        BufferedReader br;
        try{
            String temp;
            br= new BufferedReader(new FileReader("ct.txt"));//sensor data reading here
            while((temp=br.readLine())!=null)
            {
                save=save+temp+" ";
            }
            br.close();
        }
        catch(IOException xc)
        {
            xc.printStackTrace();
        }

        /*converting string to readable format and saving it in data.txt*/
        try
        {
            String[] tokens=save.split("7E");
            BufferedWriter write=new BufferedWriter(new FileWriter("data.txt"));
            
            /*we only take the lastest data frame and check after every 1 second*/
            if(tokens.length-2>=0)
            {
                String[] tokens2=tokens[tokens.length-2].split(" ");
                write.write("7E");
                write.newLine();
                for(int i=1; i<tokens2.length;i++)
                {
                    write.write(tokens2[i]);
                    write.newLine();
                }
            }
            write.close();
            myfunc();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        dataFix();
    }

    public static void GetAllInfo(XbeeData xb)
    {
        /*Interpreting data*/
        String temp="", out="Data: ";
        if(xb.digitalData[2]==1 && xb.digitalData[1]==0 && xb.digitalData[0]==0)
        {
            out=out+"Low";
            temp="L";
        }
        else if(xb.digitalData[2]==1 && xb.digitalData[1]==1 && xb.digitalData[0]==0)
        {
            out=out+"Medium";
            temp="M";
        }
        else if(xb.digitalData[2]==1 && xb.digitalData[1]==1 && xb.digitalData[0]==1)
        {
            out=out+"High";
            temp="H";
        }
        else if(xb.digitalData[2]==0 && xb.digitalData[1]==0 && xb.digitalData[0]==0)
        {
            out=out+"No Reading";
            temp="N";
        }
        else
        {
            out=out+"Invalid Data";
            temp="X";
        }
        
        /*measruing program runtime*/
        cur_avg=counter*cur_avg+time_ms;
        counter++;
        cur_avg=cur_avg/counter;
        cur_avg=Math.round(cur_avg*1000.0)/1000.0;
        
        /*updates data*/
        l1.setText("*******************DATA RECIEVED*******************");
        l2.setText("Sensor ID: "+xb.id);
        l3.setText(out);
        l4.setText("Average Time: "+cur_avg+"ms");
        myFrame.setResizable(false);
        myFrame.pack();
        
        try
        {
            DateFormat DF=new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            Date date = new Date();
            BufferedWriter pw=new BufferedWriter(new FileWriter("log.txt",true));//this format writes without overwriting the file
            pw.write(xb.id+" "+temp+" "+time_ms+"ms "+cur_avg+"ms(avg) "+DF.format(date));
            pw.newLine();
            pw.close();
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }

    public static void myfunc()
    {
        /*starting timer*/
        double start=System.nanoTime();
        
        /*getting data into program from data.txt*/
        XbeeData xb=new XbeeData();
        BufferedReader br;
        try{
            br= new BufferedReader(new FileReader("data.txt"));
            xb=Library.test(br);
            br.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        /*calculating runtime*/
        double end=System.nanoTime();
        time_ms=Math.round((end-start)/1000.0)/1000.0;
        
        /*output*/
        GetAllInfo(xb);
    }

    public static void main(String[] args){
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p1=new JPanel();
        p1.setLayout(new GridLayout(4,1,20,20));
        p1.add(l1);
        p1.add(l2);
        p1.add(l3);
        p1.add(l4);
        p1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        myFrame.add(p1);
        myFrame.pack();
        myFrame.setLocationByPlatform(true);
        myFrame.setVisible(true);
        dataFix();
    }
}