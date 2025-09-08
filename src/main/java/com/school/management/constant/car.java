package com.school.management.constant;

import java.util.Stack;


class car
{

    public static void main(String[] args) {
        int arr[] = {1,3,4,3,5,7,2,8,0};
        int maxNumber = Integer.MIN_VALUE;
        int secondMax = Integer.MIN_VALUE;
        for(int i = 0 ; i <arr.length; i++)
        {
            if( maxNumber< arr[i])
            {
               secondMax=maxNumber;//7
                maxNumber =arr[i];//8
            }
            else if( secondMax<arr[i] && arr[i]<maxNumber)
            {
                secondMax =arr[i];
            }
        }
        System.out.println(secondMax);



    }
}