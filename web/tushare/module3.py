import tushare as ts
import pandas as pd
import os

def cal(df):
    print(len(df))

if __name__ == '__main__':
    print("模型3计算")
    dir = "module"
    for name in os.listdir(dir):
        file = f"{dir}/{name}"
        if not os.path.exists(file) or not os.path.isfile(file):
            # 判断文件是否存在
            continue

        try:
            df = pd.read_csv(file)
            if df.empty:
                # 判断数据是否为空
                continue
            else:
                cal(df)
        except Exception as e:
            print(e)




