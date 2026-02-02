import os

import tushare as ts
import pandas as pd
import time

import ZukTuShare

# 初始化pro接口
pro = ZukTuShare.getPro()

def download_limit_list_d(trade_date):
    # 拉取数据
    df = pro.limit_list_d(**{
        "trade_date": trade_date,
        "ts_code": "",
        "limit_type": "",
        "exchange": "",
        "start_date": "",
        "end_date": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "trade_date",
        "ts_code",
        "industry",
        "name",
        "close",
        "pct_chg",
        "amount",
        "limit_amount",
        "float_mv",
        "total_mv",
        "turnover_ratio",
        "fd_amount",
        "first_time",
        "last_time",
        "open_times",
        "up_stat",
        "limit_times",
        "limit"
    ])
    print(df)
    return df

def save_limit_list_d(trade_date):
    df = download_limit_list_d(trade_date)
    df.to_csv(f"data/{trade_date}_limit_list_d.csv")

# 获取炸板数据
if __name__ == '__main__':
    trade_date = ZukTuShare.analysis_trade_date
    save_limit_list_d(trade_date)