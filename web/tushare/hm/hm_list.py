
# 导入tushare
import tushare as ts
import ZukTuShare
import pandas as pd

# 初始化pro接口
pro = ZukTuShare.getPro()

def download_hm_list():
    # 拉取数据
    df = pro.hm_list(**{
        "name": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "name",
        "desc",
        "orgs"
    ])
    # print(df)
    return df


if __name__ == '__main__':
    print("获取市场游资名录")
    df = download_hm_list()
    df.to_csv("hm_list.csv", encoding="utf-8", index=False)
    print("完成")


