#!/usr/bin/env python3
# hello.py - 一个简单的测试脚本

def main():
    print("Hello, World!")
    print("Python脚本执行成功！")
    
    import datetime
    print("当前时间: {}".format(datetime.datetime.now()))
    
    # 添加一些测试功能
    numbers = [1, 2, 3, 4, 5]
    total = sum(numbers)
    print("数字列表: {}".format(numbers))
    print("总和: {}".format(total))
    
    return 0

if __name__ == "__main__":
    main()