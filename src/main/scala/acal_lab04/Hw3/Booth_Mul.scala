package acal_lab04.Hw3
//import acal_lab04.Hw1._
import chisel3._
import chisel3.util._
import scala.annotation.switch

//------------------Radix 4---------------------
class Booth_Mul(width:Int) extends Module {
  val io = IO(new Bundle{
    val in1 = Input(UInt(width.W))      //Multiplicand
    val in2 = Input(UInt(width.W))      //Multiplier
    val out = Output(UInt((2*width).W))
  })
  // width/2 mul_section
  val mul_section = Wire(Vec(width/2, SInt(32.W)))
  val B = Wire(UInt((width).W))
  B := io.in1
  val A = io.in2.asSInt()

  for (i <- 0 until width/2) {
    mul_section(i) := 0.S(32.W)
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
  
  // 加總32bits, ignore overflow bits

  var ans = 0.S((32).W)
  for (i <- 0 until mul_section.length) {
    ans = ans +& mul_section(i)
  }
  //output Uint
  io.out := ans.asUInt()

}

