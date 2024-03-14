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
  //io.out := 0.U
  //io.o_f := false.B
  val fa_array = Array.fill(4)(Module(new FullAdder).io)
  val S = Wire(Vec(4,UInt()))
  fa_array(0).A := io.in_1(0)
  fa_array(0).B := io.op ^ io.in_2(0)
  fa_array(0).Cin := io.op
  S(0) := fa_array(0).Sum
  
  for (i <- 1 until 4) {
    fa_array(i).A := io.in_1(i)
    fa_array(i).B := io.op ^ io.in_2(i)
    fa_array(i).Cin := fa_array(i - 1).Cout
    S(i) := fa_array(i).Sum
  }
  val f_flag = (fa_array(2).Cout ^ fa_array(3).Cout) =/= 0.U

  
  //S(4):= Mux(f_flag, S(3),fa_array(3).Cout)
  io.o_f := f_flag
  io.out := S.asUInt
  
}
