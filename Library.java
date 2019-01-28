/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import java.io.*;
import java.io.IOException;

public class Library{
        
        private static int length_digital=0, length_analog=0;
	private static XbeeData V;
	
	public static void stayCool(int miliSec){//basic sleep function
		long end=System.currentTimeMillis()+miliSec;
		while(System.currentTimeMillis()<end);
	}

	public static String addSpace(String s, int l){ 
		String d="";                           
		for(int i=0; i<l-s.length(); i++)
			d+="0";
		d+=s.toLowerCase();
		return d;
	}

	public static int convert(String s[], int st, int e){
		int v=0;
		for(int i=st; i<st+e; i++){          
			v*=256;                     
			v+=Integer.parseInt(s[i], 16);
		}
		return v;
	}

	public static int countBit(int value, int limit){
		int count=0;
		for(int i=0; i<limit; i++){
			if(((value>>i)&1)==1)
				count++;	
		}
		return count;
	}
	
	public static XbeeData test(BufferedReader in) throws IOException{
            V=new XbeeData();                                             
            String line;
            int offset=0;
            while((line=in.readLine())!=null){
                    
                V=new XbeeData();
                    
                int frameLength=0;
                for (int i=0; i<2; i++){
                        frameLength*=256;
                        frameLength+=Integer.parseInt(in.readLine().toLowerCase(),16);
                }
                
                Integer a[];
                String s[]=new String[frameLength+1];
                for (int i=0; i<=frameLength; i++){
                        s[i]=in.readLine();
                }
                
                /***Getting  Network ID***/
                a=new Integer[1];
                a[0]=(convert(s,5,4));
                V.setId(a[0]);
                /***Getting  Digital IO Mask***/
                int digiMask=convert(s,13,2);
                /***Getting  Analog IO Mask***/
                int analogMask=convert(s,15,1);
                /***Getting  Digital Value***/
                length_digital=countBit(digiMask,16);
                a=new Integer[length_digital];
                if(a.length!=0){
                        offset=2;
                        int digi=convert(s,16,2);
                        for(int i=0, c=0; i<16; i++)
                                if(((digiMask>>i)&1)==1){
                                        a[c++]=((digi>>i)&1);
                                }
                }
                V.setDigitalData(a);

                /***Getting  Analog Value***/
                length_analog=countBit(analogMask,8);
                a=new Integer[length_analog];
                for(int i=0, c=0; i<8; i++){
                        if(((analogMask>>i)&1)==1){
                                a[c++]=(convert(s,16+offset,2));
                                offset+=2;
                        }
                }
                V.setAnalogData(a);

                /***Checking Checksum***/
                int total=0;
                for(int i=0; i<=frameLength; i++)
                        total+=Integer.parseInt(s[i], 16);

                String cs=Integer.toHexString(total);
                if(cs.substring(cs.length()-2).equalsIgnoreCase("ff")) break;	
                System.out.println("CheckSum ERROR "+cs);
            }
            if(!V.ok())
                System.out.println("CheckSum Passed-->"+V.ok());
            
            return V;
	}


	public static String calculateChkSum(String []s){

		int sum=0;
		for(int i=3; i<s.length-1; i++)
			sum+=Integer.parseInt(s[i],16);
		String s1=Integer.toHexString(1279-sum); ///4FF-SUM
		return s1.substring(s1.length()-2).toLowerCase();
	}
}
