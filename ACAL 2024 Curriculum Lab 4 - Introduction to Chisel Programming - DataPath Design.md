---
title: ACAL 2024 Curriculum Lab 4 - Introduction to Chisel Programming - DataPath Design
robots: noindex, nofollow
---

# <center>ACAL 2024 Curriculum Lab 4 <br /><font color="＃1560bd">Introduction to Chisel Programming<br />DataPath Design</font></center>

###### tags: `AIAS Spring 2024`

[toc]




## Chisel Tutorial
- [ACAL 2024 Curriculum Lab 4-0 Chisel Environment Setup and Template Tutorial](https://course.playlab.tw/md/iV5YvgW6RJiZ3LUxGSdUsw)
這份文件詳述了Chisel 的內容，請大家先看完這份文件，完成工作環境的建置跟測試。

## Introduction
- 在課堂上，介紹了...
    - 邏輯(AND、OR、XOR...)
    - 布林表示式以及如何簡化
    - Combinational Circuit以及Sequential Circuit
- 那在本次Lab中你會學習到：
    - 一些經典的Combinational logics 的練習，體會每個運算元背後的電路原理。
    - chisel的撰寫方式與技巧。
        - Hierarchical Implementation
        - Hardware Generator
    - 使用上周提供的Blocks，加上pc以及decoder，實現能支援單一指令的 datapath 設計。

## Combinational Circuit
- 只由邏輯閘組合而成的電路。
- 沒有記憶功能，不會也不應該產生任何的Flip Flop
- 設計思維(以Half-Adder為例)：
    ![](/uploads/upload_0aac07b45185ae2fd8acf663d585733a.png)
    - Truth Table
        - 將電路所需的I/O ports列舉出來，並根據所有的Input ports的組合，寫出期望的Output值。整理成上方右邊那張表格。
    - Boolean Algebra and Simplification
        - $Sum = A'B + AB' = A\ xor\ B$
        - $Carry = AB$
    -  Gate-Level Circuit for programming
        - 將表示式繪製成電路圖，就會得到上方左邊的電路圖
    - chisel Example：
    ```scala=
    class HalfAdder extends Module{
      val io = IO(new Bundle{
        val A = Input(UInt(1.W))
        val B = Input(UInt(1.W))
        val Sum = Output(UInt(1.W))
        val Carry = Output(UInt(1.W))
      })
      //the behavior of circuit
      io.Sum := io.A ^ io.B
      io.Carry := io.A & io.B
    }
    ```
Lab 4
===
Lab4-0 : Environment and Repo Setup
---
- Build Course docker and bring up a docker container
    - 在開始lab之前必須先將課堂的docker container run起來，並把一些環境建好，可以參考下面的tutorial : [Lab 0 - Course Environment Setup](https://course.playlab.tw/md/33cXunaGSdmYFej1DJNIqQ)

:::warning
- You may setup passwordless ssh login if you like. Please refer to [Use SSH keys to communicate with GitLab](https://docs.gitlab.com/ee/user/ssh.html)
- Also, if you would like to setup the SSH Key in our Container. Please refer to this [document](https://course.playlab.tw/md/CW_gy1XAR1GDPgo8KrkLgg#Set-up-the-SSH-Key) to set up the SSH Key in acal-curriculum workspace.
:::

```shell=
## bring up the ACAL docker container 
## clone the lab04 files
$  cd ~/projects
$  git clone ssh://git@course.playlab.tw:30022/acal-curriculum/lab04.git
$  cd lab04

## show the remote repositories 
$  git remote -v
origin	ssh://git@course.playlab.tw:30022/acal-curriculum/lab04.git (fetch)
origin	ssh://git@course.playlab.tw:30022/acal-curriculum/lab04.git (push)

## add your private upstream repositories
## make sure you have create project repo under your gitlab account
$  git remote add gitlab ssh://git@course.playlab.tw:30022/<your ldap name>/lab04.git

$  git remote -v
gitlab	ssh://git@course.playlab.tw:30022/<your ldap name>/lab04.git (fetch)
gitlab	ssh://git@course.playlab.tw:30022/<your ldap name>/lab04.git (push)
origin	ssh://git@course.playlab.tw:30022/acal-curriculum/lab04.git (fetch)
origin	ssh://git@course.playlab.tw:30022/acal-curriculum/lab04.git (push)
```

- When you are done with your code, you have to push your code back to your own gitlab account with the following command :
```shell=
## the first time
$  git push --set-upstream gitlab main
## after the first time
$  git fetch origin main
## remember to solve conflicts
$  git merge origin/main
## then push back to your own repo
$  git push gitlab main
```

Lab4-1 : Full Adder
---
### Introduction
- 電路圖&Truth Table
![](https://course.playlab.tw/md/uploads/160c0f8a-5f16-4a1f-af63-de75552a583f.png =40%x) ![](https://course.playlab.tw/md/uploads/6111fb11-8550-4469-8bc9-d14c9fbfc131.png =35%x)

- 設計思維：
    - HalfAdder考慮的輸入只有一個位的bit相加。
    - FullAdder做bit相加時，考慮了前一位進位(Cin)，並將這一位的進位(Cout)傳給下一位元。 
    - Reference：*chisel-tutorial/src/examples/FullAdder.scala*
        - 在chisel-tutorial提供的範例中，sum和cout是直接藉由Boolean Algebra推導寫出。
        ```scala=
        // Generate the sum
        val a_xor_b = io.a ^ io.b
        io.sum := a_xor_b ^ io.cin
        // Generate the carry
        val a_and_b = io.a & io.b
        val b_and_cin = io.b & io.cin
        val a_and_cin = io.a & io.cin
        io.cout := a_and_b | b_and_cin | a_and_cin
        ```
### Module Hierarchy
- 說明：大型的電路模組可以由小塊的模組組合而成。
- 現在我們試著利用剛剛寫出的Half_Adder組合出Full_Adder。
:::info
- 引用的方式：
    - 欲使用的module如果在**同個package**的話，直接宣告即可。
    - 若不是，則必須引入該module所在的package。
    ```scala=
    import {package}._ //在不同package時才需要。
    ...
    val ha = Module(new HalfAdder()) //Module的引入方式。 
    ...
    ```
:::
- Lab-4.1 Code
    ```scala=
    class FullAdder extends Module{
      val io = IO(new Bundle{
        val A = Input(UInt(1.W))
        val B = Input(UInt(1.W))
        val Cin = Input(UInt(1.W))
        val Sum = Output(UInt(1.W))
        val Cout = Output(UInt(1.W))
      })

      //Module Declaration
      val ha1 = Module(new HalfAdder())
      val ha2 = Module(new HalfAdder())

      //Wiring
      ha1.io.A := io.A
      ha1.io.B := io.B

      ha2.io.A := ha1.io.Sum
      ha2.io.B := io.Cin

      io.Sum := ha2.io.Sum
      io.Cout := ha1.io.Carry | ha2.io.Carry
    }
    ```
- Tester Code
    - 撰寫思維：將所有可能的input pairs都做為測試對象，將預期輸出透過expect()或者assert()來比對。
    ```scala=
    class FullAdderTest (fa : FullAdder) extends PeekPokeTester(fa){
      for(a <- 0 until 2){
        for(b <- 0 until 2){
          for(c <- 0 until 2){
            poke(fa.io.A,a)
            poke(fa.io.B,b)
            poke(fa.io.Cin,c)

            var x = c & (a^b)
            var y = a & b

            expect(fa.io.Sum,(a^b^c))
            expect(fa.io.Cout,(x|y))
            step(1)
          }
        }
      }
      println("FullAdder test completed!!!")
    }
    ```
    - 接下來一樣準備入口函式。
    ```scala=
    object FullAdderTest extends App{
      Driver.execute(Array("-td","./generated","-tbn","verilator"),() => new FullAdder()){
        c => new FullAdderTest(c)
      }
    }
    ```
    - 在shell內下指令
    ```shell=
    ## in sbt shell
    $ sbt 'Test/runMain acal_lab04.Lab.FullAdderTest'
    ```
:::warning
- Debugging
    - 重點不是sucess，而是要看倒數第二行是PASSED還是FAILED喔!!!
        - by PeekPokeTester
            - 正確
            ![](https://course.playlab.tw/md/uploads/ff175a4d-427e-4b39-9b53-6da0f958a7be.png)
            - 錯誤
            ![](https://course.playlab.tw/md/uploads/57a4e685-44d6-4092-8bc6-3a76dbad1cce.png)
        - by VCD file
            ![](https://course.playlab.tw/md/uploads/43cbdd09-4b91-45d1-aac4-7c1689985e93.png)
:::
## Lab4-2 : 32-bits Ripple Carry Adder
### Introduction
- 設計思維：利用32個Full-Adder串接，實驗32bits的加法器。
- Example：4-bit RCAdder
    - 圖片擷取自：[Analysis of Basic Adder with Parallel Prefix Adder : Fig.1](10.1109/ICMICA48462.2020.9242842) ![](https://course.playlab.tw/md/uploads/e44fcb03-3278-4045-92a5-47c18c4b78a4.png)

### Hardware Generator
- 只利用Module Hierarchy去完成，會出現以下的問題。
    - 重複出現的電路，接線接得很煩。
    - 規格一變，努力白費。
- 使用時機
    - 電路重複性高、規格不確定時，可以等到synthesis的時候再藉由傳入參數來決定就好。

- Lab4-2 Code
    ```scala=
    class RCAdder (n:Int) extends Module{
      val io = IO(new Bundle{
          val Cin = Input(UInt(1.W))
          val In1 = Input(UInt(n.W))
          val In2 = Input(UInt(n.W))
          val Sum = Output(UInt(n.W))
          val Cout = Output(UInt(1.W))
      })

      //FullAdder ports: A B Cin Sum Cout
      val FA_Array = Array.fill(n)(Module(new FullAdder()).io)
      val carry = Wire(Vec(n+1, UInt(1.W)))
      val sum   = Wire(Vec(n, Bool()))

      carry(0) := io.Cin

      for (i <- 0 until n) {
        FA_Array(i).A := io.In1(i)
        FA_Array(i).B := io.In2(i)
        FA_Array(i).Cin := carry(i)
        carry(i+1) := FA_Array(i).Cout
        sum(i) := FA_Array(i).Sum
      }

      io.Sum := sum.asUInt
      io.Cout := carry(n)
    }
    ```
    :::warning
    Q：**Line 25**...為什麼不用 io.Sum(i)逐一bit進行賦值就好？
    A：Chisel3 does not support **subword assignment**. The reason for this is that subword assignment generally hints at a better abstraction with an aggregate/structured types, i.e., a Bundle or a Vec.     
    ::: 
    - Instruction
        ```shell=
        $ bash build.sh acal_lab04.Lab RCAdder 32
        ## 32為傳入class的參數，意思為指定生成32bits的漣波加法器。
        ```
- Tester code
  ```scala=
  class RCAdderTest (dut:RCAdder) extends PeekPokeTester(dut){
    //另類的作法，只針對某些case去進行測試。
    val in1 = Array(5,32,1,77,34,55,12)
    val in2 = Array(3456,89489,78,5216,4744,8,321)

    //in1.zip(in2).foreach{
    (in1 zip in2).foreach{
      case(i,j)=>
         poke(dut.io.In1,i)
         poke(dut.io.In2,j)
         expect(dut.io.Sum,i+j)
    }
    println("RCAdder test completed!!!!!")
  }
  ```
    - 入口函式 
    ```scala=
    object RCAdderTest extends App{
        Driver.execute(args,()=>new RCAdder(32)){
            c => new RCAdderTest(c)
        }
    }    
    ```
    - Instruction
    ```shell=
    $ sbt 'Test/runMain acal_lab04.Lab.RCAdderTest -tbn verilator -td ./generated'
    ```
## Lab4-3 : Carry Lookahead Adder
### Introduction
- 設計思維：
    - 倚賴RCAdder的缺點就是，從**輸入資料進來**到**答案產生**最後一個FAdder的Cout所花的時間會是最長且和bit數(n)相關。
        - 如果一個FAdder的Cout產生的花費時間是t，那串接後的最後一個Cout會需要n*t時間。
        :::info
        我們會稱呼花費時間最長的路徑叫做 **Critical Path**
        :::
    - 透過提前分析input資料，我們可以提前算出每一位bit的Cin，加快運行速度->讓每一bit的加法能夠同時進行，而不用等前一位的Cout。
    - **Tradeoff**:越後面的Carry它的展開式會越複雜，等於是**用空間去換取時間**的一種做法，不建議做太多bit，你也不會想一直展開。
- Example：4-bit CLAdder
    - 圖片來源：[Gate Vidyalay](https://www.gatevidyalay.com/carry-look-ahead-adder/logic-diagram-of-carry-look-ahead-adder-1/) 
    ![](https://course.playlab.tw/md/uploads/9e27737a-aaf1-4e1d-8b17-5bca6f07ec1d.png  =75%x)

:::success
[**公式推導**]
$$
    Cout=(A．B)+(A．C_{in})+(B．C_{in}) \\
    = (A．B)+(A+B)．C_{in} \\
    = G + P．C_{in} \\
    ------------------- \\
    G_{i}:Generate=A_{i}．B_{i} \\
    P_{i}:Propagate=A_{i}+B_{i} \\
    C_{i+1} = G_{i}+P_{i}．C_{i}
$$
:::
- Lab4-3 Code
    ```scala=
    class CLAdder extends Module{
      val io = IO(new Bundle{
          val in1 = Input(UInt(4.W))
          val in2 = Input(UInt(4.W))
          val Cin = Input(UInt(1.W))
          val Sum = Output(UInt(4.W))
          val Cout = Output(UInt(1.W))
      })

      val P = Wire(Vec(4,UInt()))
      val G = Wire(Vec(4,UInt()))
      val C = Wire(Vec(4,UInt())) 
      val S = Wire(Vec(4,UInt()))

      for(i <- 0 until 4){
          G(i) := io.in1(i) & io.in2(i)
          P(i) := io.in1(i) | io.in2(i)
      }

      C(0) := io.Cin
      C(1) := G(0)|(P(0)&C(0))
      C(2) := G(1)|(P(1)&G(0))|(P(1)&P(0)&C(0))
      C(3) := G(2)|(P(2)&G(1))|(P(2)&P(1)&G(0))|(P(2)&P(1)&P(0)&C(0))

      val FA_Array = Array.fill(4)(Module(new FullAdder).io)

      for(i <- 0 until 4){
          FA_Array(i).A := io.in1(i)
          FA_Array(i).B := io.in2(i)
          FA_Array(i).Cin := C(i)
          S(i) := FA_Array(i).Sum
      }

      io.Sum := S.asUInt
      io.Cout := FA_Array(3).Cout
    }
    ```
- Tester code
    ```scala=
    class CLAdderTest (dut:CLAdder) extends PeekPokeTester(dut){
        for(i <- 0 to  15){
            for(j <- 0 to 15){
                poke(dut.io.in1,i)
                poke(dut.io.in2,j)
                if(peek(dut.io.Cout)*16+peek(dut.io.Sum)!=(i+j)){
                    println("Oh No!!")
                }
            }
        }
        println("CLAdder test completed!!!")
    }
    ```
    - 入口函式
    ```scala=
    object CLAdderTest extends App{
        Driver.execute(args,()=>new CLAdder){
            c => new CLAdderTest(c)
        }
    }
    ```
    - Instruction
    ```shell=
    $ sbt 'Test/runMain acal_lab04.Lab.CLAdderTest -tbn verilator -td ./generated'
    ```
## Lab4-4 : RV32I datapath realization (ADDI for example) 
### Introduction
- 同學可以參考 [這份投影片](https://inst.eecs.berkeley.edu/~cs61c/su20/pdfs/lectures/lec12.pdf)(*source：CS61C 2020 Summer Lec12*)
- 圖片來源：[CS61C 2020 Summer Lec12 : P.54](https://inst.eecs.berkeley.edu/~cs61c/su20/pdfs/lectures/lec12.pdf)
    ![](https://course.playlab.tw/md/uploads/cde3227f-519f-4b7a-83f3-e224844f26d5.png =70%x) 
:::info
- 檔案位置存放在Hw4資料夾中，而非Lab喔!!
- 在下方程式碼中可能看到以下註解：
    - //Please fill in the blanks by yourself
        - 代表這裡我刪掉了一些和lab(I-type implementation)無關的部分，但和其他type還是有關，同學要自行加入。
    - //Please correct 0.U by yourself
        - 部分常數由於需要讓同學填，但又得求compile過，所以暫時改成0，請同學修正至正確。
    - //Half-baked version
        - 代表這個訊號目前只考慮某個面向，若要因應所有的type，勢必得加入些條件判斷才行。
    - //註解掉某些信號
        - 詳情請往下找Decoder的部分。
:::

### I-type Instruction realization
- I-type Instruction的Datapath會需要...
    - PC、InstMem、Decoder、RegFile、ALU、top(for module wiring)
    1. **Program Counter**
        - 每次posedge都會+4的暫存器。
            ```scala=
            class PC extends Module {
                val io = IO(new Bundle{
                    val brtaken = Input(Bool()) 
                    val jmptaken =  Input(Bool())
                    val offset = Input(UInt(32.W))
                    val pc = Output(UInt(32.W))
                })

                val pcReg = RegInit(0.U(32.W))
                pcReg := pcReg + 4.U //Half-baked version
                io.pc := pcReg
            }
            ```
    2. **InstMem**
        - Line7：用Binary的格式讀取位於src/main/resource/InstMem.txt作為Initial Value。
            ```scala=
            class InstMem extends Module {
              val io = IO(new Bundle {
                val raddr = Input(UInt(7.W))
                val rdata = Output(UInt(32.W))
              })
              val memory = Mem(32, UInt(32.W))
              loadMemoryFromFile(memory, "./src/main/resource/InstMem.txt",MemoryLoadFileType.Binary)

              io.rdata := memory((io.raddr>>2))
            }
            ```
        - InstMem.txt的介紹
            - 此檔案為提供InstMem初始值的文件，在lab04/src/main/resourse資料夾內你會看到...
                - InstMem_wc.txt (wc = with comment，寫給自己和助教看的)
                - InstMem_ass.txt (切割出來的assembly code)
                - InstMem.txt (切割出來的machine code)
                - m.py (透過此python檔，進行切割。)
            - 同學可以藉由m.py將有comment的那份文件轉成Machine Code組成的InstMem.txt，作為cpu的**測試檔案**；以及方便同學做識別的InstMem_ass.txt。
            - 比較兩份文件內容
                - InstMem_wc.txt (just an example)
                    ```
                    //wc: with comment
                    //R-type testing
                    0000000 11000 10001 000 00010 0110011 //add x2 x17 x24
                    0100000 00101 00100 000 00110 0110011 //sub x6 x4 x5
                    0000000 01100 00010 001 00010 0110011 //sll x2 x2 x12
                    ```
                - InstMem.txt
                    ```
                    00000001100010001000000100110011
                    01000000010100100000001100110011
                    00000000110000010001000100110011
                    ```
                - InstMem_ass.txt
                    ```
                    add x2 x17 x24
                    sub x6 x4 x5
                    sll x2 x2 x12
                    ```
        :::info
        1. 寫測值進InstMem_wc.txt
            - "//"作為註解符號
            - machine code 有空格沒關係
        2. 執行.py檔
           ```shell=
           $ python3 ./src/main/resource/m.py
           ```
           - 提醒：machine code 和 assembly code是沒有關係的，同學必須自行確認前後有無相互呼應。
        :::
    3. **Decoder**
        - 身兼controller和ImmGen的工作，作為指令分配、拆解組合的角色。
        :::info
        - 為什麼那麼多東西要註解掉?
            - Lab4只設計datapath，所以並不會將值回傳寫回(暫存器、記憶體)，**單純從Write Back的地方驗證datapath設計的正確性**。
            - 此lab只設計了一種type的指令，事實上根本就不需要controller的信號控制硬體來因應各種type，但**作業就得開始考慮該如何使用這些control signal**
        :::
        ```scala=
        object opcode_map {
            //Please correct the 0.U by yourself
            val OP_IMM    = "b0010011".U
        }
        import opcode_map._

        class Decoder extends Module{
            val io = IO(new Bundle{
                val inst = Input(UInt(32.W))

                //Please fill in the blanks by yourself
                val funct3 = Output(UInt(3.W))
                val rs1 = Output(UInt(5.W))
                //Please fill in the blanks by yourself
                val rd = Output(UInt(5.W))
                val opcode = Output(UInt(7.W))
                val imm = Output(SInt(32.W))

                // val ctrl_RegWEn = Output(Bool()) for Reg write back
                // val ctrl_ASel = Output(Bool()) for alu src1
                // val ctrl_BSel = Output(Bool()) for alu src2
                // val ctrl_Br = Output(Bool()) for branch inst.
                // val ctrl_Jmp = Output(Bool()) for jump inst.
                // val ctrl_Lui = Output(Bool()) for lui inst.
                // val ctrl_MemRW = Output(Bool()) for L/S inst
                val ctrl_WBSel = Output(Bool()) 
                //true: from alu false: from dm
            })

            //Please fill in the blanks by yourself
            io.funct3 := io.inst(14,12)
            io.rs1 := io.inst(19,15)
            //Please fill in the blanks by yourself
            io.rd := io.inst(11,7)
            io.opcode := io.inst(6,0)

            //ImmGen
            io.imm := MuxLookup(io.opcode,0.S,Seq(
                //Please fill in the blanks by yourself
                //R-type
                //I-type
                OP_IMM -> io.inst(31,20).asSInt
                //B-type
                //S-type
                //U-type
                //J-type

            ))
            //Controller
            // io.ctrl_RegWEn := ???
            // io.ctrl_ASel   := ???
            // io.ctrl_BSel   := ???
            // io.ctrl_Br     := ???
            // io.ctrl_Jmp    := ???
            // io.ctrl_Lui    := ???
            // io.ctrl_MemRW  := ???
            io.ctrl_WBSel := true.B //true: from alu , false: from dm , another source?
        }
        ```
    4. **RegFile**
        - 32個32bits的register所組成，在chisel的宣告方式較為費勁，得從Seq轉成Vec(chisel偏好的**集合**的type)再包上一層Reg
        - 加上Init代表在宣告時可以指定初始值，換句話說，同學可以透過更改Seq裡面的值來影響RF一開始的初始值好方便可以做指令測試。(程式中的方法二)
        - 記得在top宣告時要傳入參數 readPorts 喔!
        ```scala=
        class RegFile(readPorts:Int) extends Module {
          val io = IO(new Bundle{
           val wen = Input(Bool())
           val waddr = Input(UInt(5.W))
           val wdata = Input(UInt(32.W))
           val raddr = Input(Vec(readPorts, UInt(5.W)))
           val rdata = Output(Vec(readPorts, UInt(32.W)))
         })

          // 1. the reset value of all regs is zero
          // val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

          // 2. the reset value of all regs is their index
          val regs = RegInit(VecInit(Seq.range(0,32).map{x=>x.U(32.W)}))

          //Wiring
          (io.rdata zip io.raddr).map{case(data,addr)=>data:=regs(addr)}
          when(io.wen) {regs(io.waddr) := io.wdata}
          regs(0) := 0.U
        }
        ```
        - 問題討論
            - x0 Register 恆等於0的話，合成軟體會用什麼方式實現x0呢？暫存器嗎？還是？
    5. **ALU** 
        - 根據查表，XOR的funct3為100，
        
        ```scala=
        object ALU_funct3{
          val ADD_SUB = "b000".U
          //Please correct 0.U by yourself
        }

        object ALU_funct7{
          //Please correct 0.U by yourself
        }

        import ALU_funct3._,ALU_funct7._,opcode_map._

        class ALUIO extends Bundle{
          val src1    = Input(UInt(32.W))
          val src2    = Input(UInt(32.W))
          val funct3  = Input(UInt(3.W))
          val opcode  = Input(UInt(7.W))
          val out  = Output(UInt(32.W))
        }

        class ALU extends Module{
          val io = IO(new ALUIO) 
          
          //Half-baked version
          io.out := MuxLookup(io.funct3,0.U,Seq(
            ADD_SUB -> (io.src1+io.src2),
          ))

        }
        ```
        - 驗證的同時，同學可以順手完成ALU_funct3和ALU_funct7的填空
        - 問題討論
            - 哪兩種type的指令需要仰賴ALU最多的功能呢？對於其他type的指令而言，ALU的功能是？這個問題能夠幫助思考io.out之後擴充該如何撰寫喔!!
    6. **top**
        - 連接不同的sub-module
        - 把前面的module都先宣告出來(pc mem decoder rf alu)
        ```scala=
        class top extends Module {
            val io = IO(new Bundle{
                val pc_out = Output(UInt(32.W))
                val alu_out = Output(UInt(32.W))
                val rf_wdata_out = Output(UInt(32.W))
            })

            val pc = Module(new PC())
            val im = Module(new InstMem())
            val dc = Module(new Decoder())
            val rf = Module(new RegFile(2))
            val alu = Module(new ALU())

            //PC
            pc.io.jmptaken := false.B
            pc.io.brtaken := false.B
            pc.io.offset := 0.U

            //Insruction Memory
            im.io.raddr := pc.io.pc

            //Decoder
            dc.io.inst := im.io.rdata

            //RegFile
            rf.io.raddr(0) := dc.io.rs1
            rf.io.raddr(1) := 0.U
            rf.io.waddr := 0.U
            
            //Dont change in this Lab=========
            rf.io.waddr := 0.U
            rf.io.wen := false.B
            //================================


            //ALU
            val rdata_or_zero = WireDefault(0.U(32.W))
            alu.io.src1 := rf.io.rdata(0)
            alu.io.src2 := dc.io.imm.asUInt
            alu.io.funct3 := dc.io.funct3
            alu.io.opcode := dc.io.opcode

            //Data Memory

            //Branch Comparator

            //Check Ports
            io.pc_out := pc.io.pc
            io.alu_out := alu.io.out 
            io.rf_wdata_out := rf.io.wdata
        }
        ```
- 如何測試?
    1. 準備測試指令，並轉成Machine code
        - in InstMem_wc.txt
            ```
            //I-type testing
            111111111111 00101 000 00000 0010011 //addi x0 x5 -1
            ```
       - 在Lab04資料夾下執行 m.py
           ```shell=
           $ python3 ./src/main/resource/m.py
           ```
           
        - in InstMem.txt
           ```
           11111111111100101000000000010011
           ```
    2. 可以執行tester了。
        ```shell=
         $ sbt 'Test/runMain acal_lab04.Hw4.topTest'
        ```
    3. 結果
        ![](https://course.playlab.tw/md/uploads/d8d99d03-79de-44c7-83a9-6930892eec80.png)
        - 附上WBdata，是為了能應付不同type的輸出訊號可能來自不同地方。
---
:::warning
**Chisel 會把未使用的電路直接忽略 synthesis**
因此不管是在dump出的verilog檔，還是波形圖中都不會找到該訊號。範例中的C訊號在合成時，就被省略掉了。
以下為範例：

**halfAdder.scala**
```scala=
    class HalfAdder extends Module{
      val io = IO(new Bundle{
        val A = Input(UInt(1.W))
        val B = Input(UInt(1.W))
        val Sum = Output(UInt(1.W))
        val Carry = Output(UInt(1.W))
      })
      val c = WireDefault(io.A | io.B)
      //the behavior of circuit
      io.Sum := io.A ^ io.B
      io.Carry := io.A & io.B
    }
```
**HalfAdder.v**
![](https://course.playlab.tw/md/uploads/dba4a78e-19c7-4a20-b9c3-5c5446114360.png)

**因此如果需要該訊號，希望在波形圖中可以看到，可以把該訊號接到module的output port。如果是多個module包在Top裡，可以把在module中需要觀察，但被忽略未合成出來的訊號接到Top的output port。**
範例如下：

**halfAdder.Scala**
```scala=
class HalfAdder extends Module {
  val io = IO(new Bundle {
    val A = Input(UInt(1.W))
    val B = Input(UInt(1.W))
    val C = Output(UInt(1.W))
    val Sum = Output(UInt(1.W))
    val Carry = Output(UInt(1.W))
  })
  val c = WireDefault(io.A | io.B)
  // the behavior of circuit
  io.C := c
  io.Sum := io.A ^ io.B
  io.Carry := io.A & io.B
}
```
**halfAdder.v**
![](https://course.playlab.tw/md/uploads/dd18df01-d805-4844-9372-8d854ee97663.png)
:::
Homework 4
===
- **Hw4-1~3**
    - 撇除複雜度較高的除法，透過加、減、乘來了解電路設計的思維相對容易。
        - 加減法器和乘法器都可以用**組合邏輯**實現。
    - 熟練組合邏輯電路的撰寫。
- **Hw4-4**
    - 利用上面的那些Blocks，加上Decoder、PC實現CPU的Datapath。
- 學生補充
    - 如果真的對於一些語法不熟悉，可以去[Chisel的官網文件](https://www.chisel-lang.org/docs/explanations/operators)找找看，例如說類似verilog的bitwise operation、bitwise assignment等等都可以在裡面找到

HW4-1 Mix Adder
---
### Introduction
- 設計思維
    - RCAdder的Critical Path會很長；CLAdder則是會相對地占用面積(位數越高越複雜)。
    - 融合這兩種電路的特色，取得在**面積和時間上的平衡點**。
### Function Declaration
- 利用Lab使用的兩種Adder(Ripple Carry and Carry Look Ahead)，組合出一個由8個4-bit CLAdder組成的32-bit Adder。
    - Hint：用Hardware Generator的方式宣告ClAdder_Array，觀念在Lab4-2的RCAdder。
- port
    ```scala=
    // n為CLAdder個數
    class MixAdder (n:Int) extends Module{
      val io = IO(new Bundle{
          val Cin = Input(UInt(1.W))
          val In1 = Input(UInt((4*n).W))
          val In2 = Input(UInt((4*n).W))
          val Sum = Output(UInt((4*n).W))
          val Cout = Output(UInt(1.W))
      })
      //Implement Here
    }
    ```
    ```shell=
    $ sbt 'Test/runMain acal_lab04.Hw1.MixAdderTest -tbn verilator -td ./generated'
    ```

Hw4-2 Add-Suber
---
### Introduction
- 設計思維
    - 減法可以藉由提前將**加數**做二補數的轉換，讓減法一樣用加法器實現。
### Function Declaration
- 利用Lab完成的Full Adder組合出4-bits加減法器
    - overflow detector
    - 2's complement
- port
    ```scala=
    class Add_Suber extends Module{
        val io = IO(new Bundle{
            val in_1 = Input(UInt(4.W))
            val in_2 = Input(UInt(4.W))
            val op = Input(Bool()) // 0:ADD 1:SUB
            val out = Output(UInt(4.W))
            val o_f = Output(Bool())
        })
    }
    //Implement Here
    ```
    - op：operation，決定該做加法(0)還是減法(1)
    - o_f：overflow，當**正確答案**超過了有號數加法器所能表示的範圍(-8~7)時，為**high**，其餘時間為**low**
    - out：宣告為UInt，目的是為了讓同學練習不以SInt的方式實現此作業，也可以另外去參考tester裡如何將4-bits的值sign-extend成負數的方式。
- [Learning Source](https://www.youtube.com/watch?v=IAkhdYtNjb0)
```shell=
$ sbt 'Test/runMain acal_lab04.Hw2.Add_SuberTest -tbn verilator -td ./generated'
```

Hw4-3 Booth Multiplier
---
### Introduction
- 是計算機中一種利用數的2補數形式來計算乘法的技巧。
- 算法原理：
    - 考慮一個二進位乘數：$m=2'b01111100$
    - 要將其轉換成十進位，直覺來說，理應是每一位的值呈上該位的權重然後相加也就是$$2^6+2^5+2^4+2^3+2^2=124$$，以此例子而言，被乘數和乘數勢必會產生5(m裡面1的數量)個部份積(*partial sum, pp*)等著之後做位移和相加。此時被乘數和乘數有可能產生的部分積不是0就是被乘數自己。
    - 事實上，在二進制表示法之中，連續出現的1，轉而用**頭尾位數各外擴一個位數的減法替代**，是能夠減少部分積的數量進而加快電路運行的速度，以上面的m舉例而言，連續的一出現在(6到3)同樣也能表示成第7位和第2位的相減(外擴，$2'b10000-2'b00001=2'b01111$)。把需要減掉的位數改用-1表示->$2'b10000(-1)00$，一樣我們試著轉為十進制來驗算，$2^7-2^2=128-4=124$，部分積的項數由5項降低至2項，而部分積則由可能是被乘數、0、或者-1倍的自己。而這種技巧我們稱為**Radix-2**。
        - 某些位數以負數表示=>代表部分積也是會有產生負數的可能性。
        - 在Radix-4中，部分積有可能為被乘數乘以(-2)...
            - 實現方式:轉成二補數後往左shift一位。
- 證明與進階推導：
    - ![](https://course.playlab.tw/md/uploads/d69fee54-ea32-4fc2-8f01-06cf6385bbf8.png =80%x)

    - 以bit-wise的方式分析，每一個二補數都能表示成上方的第一行，$y_{-1}$設為0，只是方便演算法進行分析，和前面提到的**外擴**有一些關係。
        - 題外話：Hw4-2的tester的_signed function，就是利用這種方式將bit vector轉譯成signed integer的形式喔!!!
    - 第一個等號為Radix-2，用大小為2的sliding window去掃描連續的1出現的狀況，可能的狀況有00、01、10、11。假設乘數都沒有連續兩個0或1的情況發生，那我的部分積最多還是需要n項，Ex: $2'b010101$
       | 狀況 | 意義        | 輸出 |
       | ---- | ----------- | ---- |
       | 00   | 連續的0中間 | 0    |
       | 01   | 連續的1開始 | 1    |
       | 10   | 連續的1結束 | -1   |
       | 11   | 連續的1中間 | 0    |

    - 第二個等號為Radix-4，為更進階的分析方法，一次看三個bit來進行分析，用大小為3的sliding window，stride=2去掃描，可能的狀況提升到了8種。而部分積的可能性也提升到5種(-2~2)，但這時部分積的項數最多也會降低到$n/2$項。每一位的權重由高至低(-2,1,1)
       | 狀況 | 輸出 |
       | ---- | ---- |
       | 000  | 0    |
       | 001  | 1    |
       | 010  | 1    |
       | 011  | 2    |
       | 100  | -2   |
       | 101  | -1   |
       | 110  | -1   |
       | 111  | 0    | 
- Hint
    - 首先，應該要先將上方推導證明的式子中，依照括號將乘數分成x項。(想一想，x應該會是多少呢?)
    - x項中，最多存在幾種可能性呢?，根據這些可能性，我們需要對被乘數做什麼呢?
    - 得到部分積後，乘上權重做相加即可得到答案!!
    - <font color=#f00>切記!!!不需要宣告任何暫存器喔!!</font>
### Function Declaration
- 請同學，完成一16-bits(width = 16)的Radix-4 Booth 乘法器
- Hardware Generator的寫法，完成後，任意bit數的乘法器都能實現，但這裡width就先暫定是16。
    - port declaration
    ```scala=
    class Booth_MUL(width:Int) extends Module {
        val io = IO(new Bundle{
        val in1 = Input(UInt(width.W))      //multiplicand
        val in2 = Input(UInt(width.W))      //multiplier
        val out = Output(UInt((2*width).W)) //product
    })
    //Implement Here
    ```
```shell=
$ sbt 'Test/runMain acal_lab04.Hw3.Booth_MulTest -tbn verilator -td ./generated'
```  
  
  
Hw4-4 Datapath Implementation
---
- 示意圖
    - 圖片來源：[CS61C 2020 Summer Lec12 : P.54](https://inst.eecs.berkeley.edu/~cs61c/su20/pdfs/lectures/lec12.pdf)
![](https://course.playlab.tw/md/uploads/f8838c0b-a883-4ca2-aee1-b245c910e9e3.png =70%x)

:::info
- 此項作業較為複雜，主要分種兩種類別
    1. 該項作業有自己的tester，代表希望同學能實現該Module的設計，並通過unit test，以順利進行後續設計。
    2. 該項作業tester為topTest，同學必須自行準備測資，以及修正在top.scala的module的接線，同學自行驗證指令功能性。
        - 記住Datapath三大原則
            - pc不要跳，+4就好
            - reg不要寫，看看就好
            - mem不要寫，看看就好
:::
### Introduction
- **PC**：指令計數器，由於Memory是以Byte為存儲單位，但指令以Word為單位(4 Bytes)，所以以每週期以**PC+4**為主，唯有在遇到Branch、Jump系列指令才會遇到**PC+offset**。
- **Decoder**：指令解碼，如字面義，Input為32bits指令，要做的事情有...
    1. 將後續模塊需要的輸入節錄"指令的部分"給他們(ex.raddr、funct、opcode...)
        - RISC-V base instruction formats showing immediate variants:
            -  圖片來源: [The RISC-V Instruction Set Manual v2.2 : P.12 Figure2.3](https://riscv.org/wp-content/uploads/2017/05/riscv-spec-v2.2.pdf)
        ![](https://course.playlab.tw/md/uploads/c6ef5ded-7421-4318-a198-daed00f7746c.png)
    2. 根據不同type的指令，去組合(Concatenate、Shift、Sign-extend)出對應的immediate。(細分可以說是ImmGen的工作...)
        - Types of immediate produced by RISC-V instructions
            -  圖片來源: [The RISC-V Instruction Set Manual v2.2 : P.12 Figure2.4](https://riscv.org/wp-content/uploads/2017/05/riscv-spec-v2.2.pdf)
        ![](https://course.playlab.tw/md/uploads/686a904d-7393-4f37-a0c9-2af0cd54b207.png)
    3. 根據opcode來決定Control Signal(細分可以說是Controller的工作...)
        -  RISC-V base opcode map, inst[1:0]=11
            - 圖片來源:  [The RISC-V Instruction Set Manual v2.2 : P.103 Table 19.1](https://riscv.org/wp-content/uploads/2017/05/riscv-spec-v2.2.pdf)
        ![](https://course.playlab.tw/md/uploads/c61ab56c-d8ab-4a86-8cbd-bef90c1c7ac8.png)
		:::info
		**補充**：在Decoder.scala的檔案中，助教有先宣告一些control的訊號(ctrl_)，這部分同學可以自己重新設計，抑或依照這些訊好去寫，最後可以讓cpu成功的work就好。
		:::
    
- **DataMemory**：
    - 作為CPU運行時，存取值的模塊。相較於InstMem的設計而言(可以視為ROM)，DataMemory的設計難點就在於必須得根據LOAD、STORE的資料大小以及地址選擇是否**對齊**(ex.存HalfWord必須得存在偶數的byte addr中。)
    - 存取長於Byte的資料時，rv32i預設以**Little-Endian**方式儲存，也就是當你愈存取一筆0xABCD1234的資料進入Memory時，看起來會像這樣...這也是需要設計的部分。
      |  0   |  1   |  2   |  3   |
      |:----:|:----:|:----:|:----:|
      | 0x34 | 0x12 | 0xCD | 0xAB |
### Hw4-4-1 I-type implementation

- tester：topTest (同學自行驗證)
- 請同學完善ALU的設計，根據Lab4-4完成以下指令。
    - addi (done)
    - slli
    - slti
    - sltiu
    - xori
    - srli
    - srai (問題討論：如何與srli做區分呢???記得讀懂兩者個規則喔!!)
    - ori
    - andi
    :::warning
    funct3有8種可能，但這裡有9條指令。
    :::

- 要做的事情有...
    - 在InstMem_wc.txt中implement你們的測資(machine & assembly)
    - 執行m.py
    - 執行tester驗證功能。
- 結果

### Hw4-4-2 R-type Implementation
- tester：topTest (同學自行驗證)
- 接續上個作業完成的ALU，接著完成R-type指令的implementation。
    | Instruction |     |     |     |     |      |     |     |     |     |     |
    |:-----------:| --- | --- | --- | --- | ---- | --- | --- | --- | --- | --- |
    | **R-TYPE**  | add | sub | sll | slt | sltu | xor | srl | sra | or  | and |
:::warning
Hint: funct3有8種可能，但這裡有10條指令。
:::
### Hw4-4-3 Complete the Decoder
- tester：DecoderTest (須通過tester)
- 要完成Deccoder，你需要做的事情有:
    1. 根據上方提供的opcode map，新增一object，以利在後續撰寫上增加程式的可讀性。
        - 要求： 以bit的方式宣告=> "b10".U
            ```scala=
            object opcode_map {
                val LOAD      = ???
                val STORE     = ???
                val BRANCH    = ???
                val JALR      = ???
                val JAL       = ???
                val OP_IMM    = ???
                val OP        = ???
                val AUIPC     = ???
                val LUI       = ???
            }
            ```
    2. 節錄指令不同部分以便往後傳給需要的模塊。
         ```scala=
         io.funct7 := ???
         io.funct3 := ???
         io.rs1    := ???
         io.rs2    := ???
         io.rd     := ???
         io.opcode := ???
         ```
    3. 不同type的指令會產生不同種立即值，根據1.所完成的opcode map，用各種方法去拼湊出屬於當前type的立即值。
        ```scala=
        import opcode_map._
        io.imm := MuxLookup(io.opcode,0.S,Seq(
            //I-type
            OP_IMM -> ???,
            JALR   -> ???,
            LOAD   -> ???,
            //B-type
            BRANCH -> ???,
            //S-type
            STORE  -> ???,
            //U-type
            LUI    -> ???,
            AUIPC  -> ???,
            //J-type
            JAL    -> ???
        ))
        ```
    4. 從opcode就知道哪些模組會派上用場，該選用哪些訊號，產生各種不同control signal.
        - 為符合此次作業僅要求同學完成Datapath的設計，已經填寫的訊號，請勿做更動。其他則讓同學自行設計功能。
        - DecoderTest.scala僅驗證同學Decoder中imm的implementation是否有誤，至於ctrl系列信號則不在驗證範圍內，應由同學自行設計規劃。
            - 舉個例子，alu_src2的來源會有兩個(reg或imm)，是不太可能限制同學說Mux select 0的時候一定得導向哪個訊號。
            ```scala=
            //Control signal
            io.ctrl_RegWEn := false.B
            io.ctrl_ASel := ???  // to choose ALU_src1
            io.ctrl_BSel := ???  // to choose ALU_src2
            io.ctrl_MemRW := 0.U // 0:Read 1:Write
            io.ctrl_WBSel := ??? // to choose wbsource
            io.ctrl_Br := ???
            io.ctrl_Jmp := ???
            io.ctrl_Lui := ???
            ```
### Hw4-4-4 Complete the PC
- tester:PCTest (須通過tester)
- 當Branch or Jump情況發生時，PC該如何對應呢?
- 要求
    - 為了預防offset與地址沒有對齊，需要同學無條件捨去offset的末兩位(改為0)
        ```scala=
        class PC extends Module {
            val io = IO(new Bundle{
                val brtaken = Input(Bool()) 
                val jmptaken =  Input(Bool())
                val offset = Input(UInt(32.W))
                val pc = Output(UInt(32.W))
            })

            val pcReg = RegInit(0.U(32.W))
            pcReg := ???
            io.pc := pcReg
        }
        ```
- **問答題**：
    - Q1：Branch情況發生時，**pc+offset**會從哪裡傳回pc呢？
    - Q2：Beq x0 x0 imm 可以完全取代 jal x0 imm嗎？
    - Q3：如果不行，為什麼?
### Hw4-4-5  Design the DataMemory
- tester:無tester，由同學自行驗證
    - 驗證答案記得要用verilator進行模擬，才可看到內部暫存器、記憶體的變化，正確答案可以參考Hw4-4-6的表格。
- 要完成Memory，你需要做的事情有...
    1. 和Decoder一樣，建立一個object以利後續程式撰寫
    2. 透過指令的哪個部分可以去分辨出以下不同的資料長度呢?
        ```scala=
        object wide {
          val Byte = ???
          val Half = ???
          val Word = ???
          val UByte = ???
          val UHalf = ???
        }
        ```
    2. 因應不同的長度，addr和data勢必也要有所更動，不論讀/寫都是。
        - 為了避免**地址不對齊**，若存值的長度為Half，須利用Lab2有用到的mask技巧，將addr的最後一位改為0，長度為Word時又該怎麼做呢??? 
        - 按照little-endian的方式去存放資料。
            ```scala=
            val wa = WireDefault(0.U(10.W)) //address
            val wd = WireDefault(0.U(32.W)) //data

            wa := MuxLookup(???,0.U(10.W),Seq(
              Byte -> io.waddr,
              Half -> ???,
              Word -> ???,
            ))

            wd := MuxLookup(???,0.U,Seq(
              Byte -> ???,
              Half -> ???,
              Word -> io.wdata,
            ))

            when(io.wen){
                when(???===Byte){
                  memory(wa) := wd(7,0)
                }.elsewhen(???===Half){
                  ???
                }.elsewhen(???===Word){
                  ???
                }
          }.otherwise{
                io.rdata := MuxLookup(???,0.S,Seq(
                  Byte -> memory(io.raddr).asSInt,
                  Half -> ???,
                  Word -> ???,
                  UByte -> ???,
                  UHalf -> ???
            ))
          }
            ```
### Hw4-4-6 Load/Store instruction Implementation
- tester：topTest (同學自行驗證)
- 接續DataMem的完成，接著完成Load/Store系列的Implementation。
- DataMem的初始值 0xaabbccdd (dd存在第0個byte喔!!!)
- 請同學完成以下指令
- for load (load rd r1 imm)
    - rd := mem(rs1+simm12)
      | instruction | assembly    | explaination       |
      | ----------- | ----------- | ------------------ |
      | lb          | lb  x1 x0 0 | x1應存放0xffffffdd |
      | lh          | lh  x1 x0 2(須為2的倍數) | x1應存放0xffffaabb |
      | lw          | lw  x1 x0 0(須為4的倍數) | x1應存放0xaabbccdd |
      | lbu         | lbu x1 x0 3 | x1應存放0x000000aa |
      | lhu         | lhu x1 x0 0 | x1應存放0x0000ccdd | 
- for store (store r2 r1 imm)
    - mem(rs1+simm12) := rs2
    - 假設x1裡面放著0x87654321
        - 題外話，要能在暫存器中放入設計好的數字，必須接續使用addi和lui來處理欲存入的imm的下半部分和上半部分。
           | inst | assembly                 | explaination                                                |
           | ---- | ------------------------ | ----------------------------------------------------------- |
           | sb   | sb  x1 x0 0              | mem(0)存入0x21                                              |
           | sh   | sh  x1 x0 2(須為2的倍數) | mem(2)存入0x21 mem(3)存入0x43                               |
           | sw   | sw  x1 x0 0(須為4的倍數) | mem(0)存入0x21 mem(1)存入0x43 mem(2)存入0x65 mem(3)存入0x87 |
- 結果：
- LOAD：
- STORE：看不見寫入資料，必須透過vcd
### Hw4-4-7 Complete the BranchComp.
- tester:BranchCompTest(須通過tester)
- 要完成BranchComp.，你需要做的事情有...
    1. 根據condition，新增一object，以利在後續撰寫上增加程式的可讀性。
        - branch系列指令是以指令的哪個部份去做區分呢?
        - 要求： 以bit的方式宣告=>2.U<font color=#f00>(X)</font> "b10".U<font color=#f00>(O)</font>
            ```scala=
            object condition{
              val EQ = ???
              val NE = ???
              val LT = ???
              val GE = ???
              val LTU = ???
              val GEU = ???
            }
            ```
    2. port declaration
        ```scala=
        class BranchComp extends Module{
            val io = IO(new Bundle{
                val en = Input(Bool())
                val funct3 = Input(UInt(3.W))
                val src1 = Input(UInt(32.W))
                val src2 = Input(UInt(32.W))

                val brtaken = Output(Bool()) //for pc.io.brtaken
            })
        }
        ```
        - en：判斷目前指令是否為B_type。是，比較結果才會影響cpu運行。
        - brtaken，branch條件是否成立。
- 問答題：
    - 在軟體上，假設我用int存取src1和src2，由於int是signed的方式存取，比大小(-1<3)自然能夠成立。有沒有什麼方式是**不需透過轉換Dtype**，就能夠實現Unsigned的比較方式呢?比如說，該如何驗證BGEU呢？
    - Hint：寫出兩個src可能組合出的4種狀況，寫truth table去分析。
### Hw4-4-8 B-type Implementation
- tester：topTest (同學自行驗證)
- 請同學完成以下指令
    | instruction |     |     |     |     |      |      |
    | ----------- | --- | --- | --- | --- | ---- | ---- |
    | B-Type      | beq | bne | blt | bge | bltu | bgeu |
- 以brtaken訊號作為驗證方式。
### Hw4-4-9 J-type Implementation
- tester：topTest (同學自行驗證)
- 請同學完成以下指令
    | instruction |     |      |
    | ----------- | --- | ---- |
    | B-Type      | jal | jalr |
- 以jmptaken、wbdata、alu out作為驗證方式。


## Homework Submission Rule
- **Step 1**
    請在自己的 GitLab內建立 `lab04` repo，並將本次 Lab 撰寫的程式碼放入這個repo。另外記得開權限給助教還有老師。
- **Step 2**
    請參考[(校名_學號_姓名) ACAL 2024 Spring Lab 4 HW Submission Template](https://course.playlab.tw/md/CSbf7XBAQbqiP7kr9MqOyg)，建立(複製一份)並自行撰寫 CodiMD 作業說明文件。請勿更動template裡的內容。
     - 關於 gitlab 開權限給助教群組的方式可以參照以下連結
        - [ACAL 2024 Curriculum GitLab 作業繳交方式說明 : Manage Permission](https://course.playlab.tw/md/CW_gy1XAR1GDPgo8KrkLgg#Manage-Permission)
- **Step 3**
    - When you are done, please submit your homework document link to the Playlab 作業中心, <font style="color:blue"> 清華大學與陽明交通大學的同學請注意選擇對的作業中心鏈結</font>
        - [清華大學Playlab 作業中心](https://nthu-homework.playlab.tw/course?id=2)
        - [陽明交通大學作業繳交中心](https://course.playlab.tw/homework/course?id=2)

