# NTHU_111065541_張騰午 ACAL 2024 Spring Lab 4 Homework Submission


[toc]

## Gitlab code link
:::info
https://course.playlab.tw/git/Tiger_Chang/lab04/-/tree/main/HW4
:::

- Gitlab link - https://course.playlab.tw/git/Tiger_Chang/lab04/-/tree/main/HW4

## Hw4-1 Mix Adder
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
package acal_lab04.Hw1
import chisel3.util._
import chisel3._
import acal_lab04.Lab._

class MixAdder (n:Int) extends Module{
  val io = IO(new Bundle{
      val Cin = Input(UInt(1.W))
      val in1 = Input(UInt((4*n).W))
      val in2 = Input(UInt((4*n).W))
      val Sum = Output(UInt((4*n).W))
      val Cout = Output(UInt(1.W))
  })
  //please implement your code below
  val clas = Array.fill(n) { Module(new CLAdder).io }
  clas(0).Cin := io.Cin//n CLAdder
  for (i <- 0 until n - 1) {
    clas(i + 1).Cin := clas(i).Cout
  }
  val S = Wire(Vec(n,UInt()))
  for (i <- 0 until n) {//每個CLAdder負責的區段
    clas(i).in1 := io.in1((i + 1) * 4 - 1, i * 4)
    clas(i).in2 := io.in2((i + 1) * 4 - 1, i * 4)
    S(i) := clas(i).Sum
  }

  
  io.Sum := S.asUInt
  io.Cout := clas(n-1).Cout
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/8286b0ad-153e-48b1-a80d-1fee99a84fd6.png)

## Hw4-2 Add-Suber
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
package acal_lab04.Hw2

import chisel3._
import chisel3.util._
import acal_lab04.Lab._

class Add_Suber extends Module{
  val io = IO(new Bundle{
  val in_1 = Input(UInt(4.W))
	val in_2 = Input(UInt(4.W))
	val op = Input(Bool()) // 0:ADD 1:SUB
	val out = Output(UInt(4.W))
	val o_f = Output(Bool())
  })

  //please implement your code below
  val fa_array = Array.fill(4)(Module(new FullAdder).io)//四個fulladder
  val S = Wire(Vec(4,UInt()))//first adder
  fa_array(0).A := io.in_1(0)
  fa_array(0).B := io.op ^ io.in_2(0)//是否做complement
  fa_array(0).Cin := io.op//for 2's complement
  S(0) := fa_array(0).Sum
  
  for (i <- 1 until 4) {
    fa_array(i).A := io.in_1(i)
    fa_array(i).B := io.op ^ io.in_2(i)
    fa_array(i).Cin := fa_array(i - 1).Cout
    S(i) := fa_array(i).Sum
  }
  val f_flag = (fa_array(2).Cout ^ fa_array(3).Cout) =/= 0.U//轉成bool值

  
  io.o_f := f_flag
  io.out := S.asUInt
  
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/a793cf75-c1a5-4c61-af01-15dce9135dd9.png)

## Hw4-3 Booth Multiplier
### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
class Booth_Mul(width:Int) extends Module {
  val io = IO(new Bundle{
    val in1 = Input(UInt(width.W))      //Multiplicand
    val in2 = Input(UInt(width.W))      //Multiplier
    val out = Output(UInt((2*width).W))
  })
  // width/2 mul_section
  val mul_section = Wire(Vec(width/2, SInt(32.W)))
  val B = Wire(UInt((width).W))
  B := io.in1//乘數
  val A = io.in2.asSInt()//被乘數

  for (i <- 0 until width/2) {
    mul_section(i) := 0.S(32.W)//部分積
  }

  // bit -1 = 0. only count bit 1,0
  switch(B(1,0)) { 
    is(0.U) {
      mul_section(0) := 0.S(32.W)//000
    }
    is(1.U) {
      mul_section(0) := A.asTypeOf(SInt(32.W))//010
    }
    is(2.U) {
      mul_section(0) := (A * (-2).S).asTypeOf(SInt(32.W))//100
    }
    is(3.U) {
      mul_section(0) := (A * (-1).S).asTypeOf(SInt(32.W))//110
    }
  }

  //other bits sections依據B的bits決定對A操作
  for (i <- 3 until width by 2) {
    switch(B(i, i-2)) {
      is(0.U) {
        mul_section((i-1)/2) := 0.S(32.W)//000->0
      }           
      is(1.U) {
        mul_section((i-1)/2) := (A << (i-1)).asTypeOf(SInt(32.W))//001->1
      }          
      is(2.U) {
        mul_section((i-1)/2) := (A << (i-1)).asTypeOf(SInt(32.W))//010->1
      }                       
      is(3.U) {
        mul_section((i-1)/2) := (A * (2).S << (i-1)).asTypeOf(SInt(32.W))//011->2
      }                    
      is(4.U) {
        mul_section((i-1)/2) := (A * (-2).S << (i-1)).asTypeOf(SInt(32.W)) //100->-2    
      }
      is(5.U) {
        mul_section((i-1)/2) := (A * (-1).S << (i-1)).asTypeOf(SInt(32.W))//101->-1
      }
      is(6.U) {
        mul_section((i-1)/2) := (A * (-1).S << (i-1)).asTypeOf(SInt(32.W))//110->-1
      }
      is(7.U) {
        mul_section((i-1)/2) := 0.S(32.W)//111->0
      }
    }
  }
  
  // 加總32bits
  var ans = 0.S((32).W)
  for (i <- 0 until mul_section.length) {
    ans = ans +& mul_section(i)
  }
  //output Uint
  io.out := ans.asUInt()
}
```
### Test Result
![](https://course.playlab.tw/md/uploads/57aa3d07-5a4d-4958-95f1-faaf44c84fb2.png)


## Hw4-4 Datapath Implementation for (3、4、7)
### PC
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `PCTest.scala` test
```
#### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

### Decoder
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `DecoderTest.scala` test
```
### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)

### BranchComp
#### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
```scala=
## scala code & comment
## make sure your have passed the `BranchCompTest.scala` test
```
#### Test Result
> 請放上你通過test的結果，驗證程式碼的正確性。(螢幕截圖即可)
 
## Hw4-4 Datapath Implementation for (1、2、6、8、9)
### InstMem_wc.txt
> 請同學放上自行準備的測資，需含有(R、I、S、B、J type測資)
```=

```

### Scala Code
> 請放上你的程式碼並加上註解(中英文不限)，讓 TA明白你是如何完成的。
- top
```scala=
## scala code & comment
## make sure you have comfirm the correctness of every type of instructions by yourself
```
### Test Result
> 請仿效文件上的截圖，放上各個type的指令驗證結果(每種type至少兩個，且與測資呼應。)
- R-type
- I-type
- S-type
- B-type
- J-type

## 文件中的問答題
- Q1:哪兩種type的指令需要仰賴ALU最多的功能呢？對於其他type的指令而言，ALU的功能是？
    - Ans1:???
- Q2:Branch情況發生時，**pc+offset**會從哪裡傳回pc呢？Beq x0 x0 imm 可以完全取代 jal x0 imm嗎？如果不行，為什麼?
    - Ans2:???
- Q3:假設我用int存取src1和src2，由於int是signed的方式存取，比大小(-1<3)自然能夠成立。有沒有什麼方式是**不需透過轉換Dtype**，就能夠實現Unsigned的比較方式呢?比如說，該如何驗證BGEU呢？(可以用pseudo code或者你認為你能表達清楚你的想法，用文字呈現也行。)
    - Ans3:???

## 意見回饋和心得(可填可不填)
