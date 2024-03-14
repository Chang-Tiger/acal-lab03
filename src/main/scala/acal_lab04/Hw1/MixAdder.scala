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
  //io.Sum := 0.U
  //io.Cout := 0.U
  val clas = Array.fill(n) { Module(new CLAdder).io }
  clas(0).Cin := io.Cin
  for (i <- 0 until n - 1) {
    clas(i + 1).Cin := clas(i).Cout
  }
  val S = Wire(Vec(n,UInt()))
  for (i <- 0 until n) {
    clas(i).in1 := io.in1((i + 1) * 4 - 1, i * 4)
    clas(i).in2 := io.in2((i + 1) * 4 - 1, i * 4)
    S(i) := clas(i).Sum
  }

  
  io.Sum := S.asUInt //Cat(clas.map(_.Sum))
  io.Cout := clas(n-1).Cout
}