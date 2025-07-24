#include <REGX51.H>

// 使用 xdata 定义一个外部 RAM 的全局数组
//unsigned char xdata buffer[128] _at_ 0x2000;

// 定义一个单个位变量，位于 bit 地址 0x20.0（P2.0）
//bit flag _at_ 0xA0;  // 0xA0 = 0x20 << 3 + 0

// 使用数据页寄存器 bank1 中断服务程序
void Timer0_ISR(void) interrupt 1 using 1 {
    static unsigned char count = 0;
    count++;
    if (count >= 100) {
        flag = !flag;       // 翻转位变量
        count = 0;
    }
}

// 初始化定时器0
void Timer0_Init() {
    TMOD &= 0xF0;       // 清除 T0 的控制位
    TMOD |= 0x01;       // 设置为模式1 (16位)
    TH0 = 0xFC;         // 设置初始值 (1000us)
    TL0 = 0x18;
    ET0 = 1;            // 开启定时器0中断
    EA = 1;             // 开启总中断
    TR0 = 1;            // 启动定时器0
}

void main() {
    Timer0_Init();

    // 写入 xdata 区域
    buffer[0] = 0x55;
    buffer[1] = 0xAA;

    while (1) {
        if (flag) {
            P1 = buffer[0];  // 输出 0x55 到 P1
        } else {
            P1 = buffer[1];  // 输出 0xAA 到 P1
        }
    }
}
