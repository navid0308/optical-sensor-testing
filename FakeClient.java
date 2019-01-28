/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class FakeClient {
	private int sensorID, networkID, dIn=3, aIn=0, state;

	ChatClient fakeClient;
	String data="7E";
	String s[];
	public FakeClient(String ip, int port, int sID, int nID, int digital, int analog, int st){
		fakeClient=new ChatClient(ip, port);
		sensorID=sID;
		networkID=nID;
		dIn=digital;
		aIn=analog;
		state=st;
		int len=1+8+2+1+1+2+1+((dIn>0)?2:0)+Library.countBit(aIn, 8)*2;
		data+=Library.addSpace(Integer.toHexString(len), 4);
		data+="92";
		data+=Library.addSpace(Integer.toHexString(sensorID),16);
		data+=Library.addSpace(Integer.toHexString(networkID),4);
		data+="0201";
		data+=Library.addSpace(Integer.toHexString(dIn), 4);
		data+=Library.addSpace(Integer.toHexString(aIn), 2);
		encode();
	}

	public void encode(){
		String d=data;
		System.out.println("State-->"+state);
		if(dIn<=0){
			return;
		}
		switch(state){
		case 1:
			d+="0001";
			break;
		case 2:
			d+="0003";
			break;
		case 3:
			d+="0007";
			break;
		default:
			d+="0000";
			break;
		}
		s=new String[(d.length()/2)+1];
		for(int i=0; i<s.length-1; i++)
			s[i]=d.substring(i*2,i*2+2);
		s[s.length-1]=Library.calculateChkSum(s);
	}

	public void changeState(int st){
		state=st;
		encode();
	}

	public void sendData() {
		for(int i=0; i<s.length; i++)
			fakeClient.sendLine(s[i]);
	}

	public static void main(String[] args) throws IOException {

		FakeClient fc=new FakeClient("localhost", 4242, 100, 100, 7, 0, 3);
		FakeClient fc1=new FakeClient("localhost", 4242, 200, 200, 7, 0, 1);
		String line=null;
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		while(true){
			if(br.ready())
				line=br.readLine();
			if(line!=null){
				int x=Integer.parseInt(line);
				if(x<0 || x>3) break;
				fc.changeState(x);
				line=null;
			}
			fc.sendData();
			fc1.sendData();
			Library.stayCool(1000);
		}
	}
}
