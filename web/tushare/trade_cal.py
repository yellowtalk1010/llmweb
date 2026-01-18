import pandas as pd
# 导入tushare
import tushare as ts
import ZukTuShare
import download_daily
import download_daily_basic
import limit_list_d
import time


# 初始化pro接口
pro = ZukTuShare.getPro_10000()

# 下载交易日期
def download_trade_cal():
    # 拉取数据
    df = pro.trade_cal(**{
        "exchange": "",
        "cal_date": "",
        "start_date": "20240101",
        "end_date": "20241231",
        "is_open": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "exchange",
        "cal_date",
        "is_open",
        "pretrade_date"
    ])
    print(df)
    df.to_csv("trade_cal.csv", index=False)


def read_trade_cal():
    trade_cals = pd.read_csv("trade_cal.csv")
    for index, row in trade_cals.sort_index(ascending=False).iterrows():
        is_open = row["is_open"]
        if(is_open==1):
            cal_date = row["cal_date"]
            print(cal_date)
            # download_daily.download_trade_date(str(cal_date))
            # download_daily_basic.download_trade_date(str(cal_date))
            # limit_list_d.save_limit_list_d(cal_date)
            time.sleep(2)



if __name__ == '__main__':
    download_trade_cal()
    # read_trade_cal()