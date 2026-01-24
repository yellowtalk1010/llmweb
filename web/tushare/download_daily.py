import os

import tushare as ts
import pandas as pd
import time
from pathlib import Path

import ZukTuShare

daily_path = "daily"

def down_load_daily(ts_code, trade_date, start_date, end_date):
    time.sleep(2)
    # 导入tushare
    # 初始化pro接口
    pro = ZukTuShare.getPro()

    # 拉取数据
    df = pro.daily(**{
        "ts_code": ts_code,
        "trade_date": trade_date,
        "start_date": start_date,
        "end_date": end_date,
        "limit": "",
        "offset": ""
    }, fields=[
        "ts_code",
        "trade_date",
        "open",
        "high",
        "low",
        "close",
        "pre_close",
        "change",
        "pct_chg",
        "vol",
        "amount"
    ])
    # print(df)
    return df

# def download_all():
#     all_stocks = pd.read_csv("all_stocks.csv")
#     print(all_stocks.__len__())
#     try:
#         for index, row in all_stocks.iterrows():
#             ts_code = row["ts_code"]
#             name = row["name"]
#             print(f"{index}, {ts_code}, {name}")
#
#             new_code = ts_code.replace(".", "_")
#             path = f"{daily_path}/{new_code}/"
#
#             if os.path.exists(path) is False:
#                 os.makedirs(path)
#
#             file = path + "/202511.csv"
#             try:
#                 if os.path.exists(file) is False:
#                     df = daily(ts_code)
#                     df.to_csv(file, index=False)
#                 print(f"{file}，成功")
#             except:
#                 os.remove(file)
#                 print(f"{file}，失败")
#     except Exception as e:
#         print(e)

# def download_one(ts_code_path, start_date, end_date):
#     ts_code = ts_code_path.replace("_", ".")
#     df = daily(ts_code, "",start_date, end_date)
#     path = f"{daily_path}/{ts_code_path}/2026.csv"
#     df.to_csv(path, index=False)
#     print(f"完成。{path}，{len(df)}")

def download_trade_date(trade_date):
    if len(trade_date) != 8:
        print("交易日期异常")
        return
    year = trade_date[0:4]
    path = f"{daily_path}/{trade_date}.csv"
    if os.path.exists(path) is False:
        trade_date_df = down_load_daily("", trade_date, "", "")
        trade_date_df.to_csv(path, index=False)
        print(f"{path}，写入成功")
    else:
        print(f"{path}，已经存在")
    flat_df = pd.read_csv(path)
    print(f"总数：{len(flat_df)}")
    # 均衡写入对应的数据年度数据中去
    merge_num = 0
    for index, flat_row in flat_df.iterrows():
        try:
            ts_code = flat_row["ts_code"]
            ts_code_path = ts_code.replace(".", "_")
            year_file = f"{daily_path}/{ts_code_path}/{year}.csv"
            Path(year_file).parent.mkdir(parents=True, exist_ok=True)

            if os.path.exists(year_file) is False:
                print(f"{year_file}，文件不存在")
                df = down_load_daily(ts_code, "", ZukTuShare.analysis_trade_date_start, trade_date)
                df.to_csv(year_file)
                continue

            # print(f"读取{year_file}")
            year_df = pd.read_csv(year_file)

            if len(year_df) > 0 and str(year_df.iloc[0]["trade_date"]) == trade_date:
                print(f"{ts_code}, {trade_date}，记录已经存在")
                continue
            else:
                print("记录不存在")
                merge = pd.concat([pd.DataFrame([flat_row.to_dict()]), year_df], ignore_index=True)
                merge = merge.loc[:, ~merge.columns.str.contains('^Unnamed')]
                merge.to_csv(year_file, index=False)
                print(f"合并成功{year_file}")
                merge_num = merge_num + 1
        except Exception as e:
            print(f"错误，{e.__str__()}")

    print(f"合并完成，{merge_num}")

if __name__ == '__main__':

    # download_one("002798_SZ", "20250101", "20251130")
    download_trade_date(ZukTuShare.analysis_trade_date)
    print("完成")




