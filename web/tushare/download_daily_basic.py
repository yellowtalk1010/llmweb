# 导入tushare
import os
from time import sleep
from pathlib import Path
import tushare as ts
import pandas as pd

import calendar
from datetime import date, timedelta

import time

# from exceptiongroup import catch

import ZukTuShare
import download_stock_basic


# 位置：数据接口->股票数据->行情数据->每日指标
def down_load_daily_basic(ts_code, trade_date, start_date, end_date):
    sleep(0.5)
    # 拉取数据
    # 初始化pro接口
    pro = ZukTuShare.getPro()
    df = pro.daily_basic(**{
        "ts_code": ts_code,
        "trade_date": trade_date,
        "start_date": start_date,
        "end_date": end_date,
        "limit": "",
        "offset": ""
    }, fields=[
        "ts_code",                      # TS股票代码
        "trade_date",                   # 交易日期
        "close",                        # 当日收盘价
        "turnover_rate",                # 换手率（%）
        "turnover_rate_f",              # 换手率（自由流通股）
        "volume_ratio",                 # 量比
        "pe",                           # 市盈率（总市值/净利润， 亏损的PE为空）
        "pe_ttm",                       # 市盈率（TTM，亏损的PE为空）
        "pb",                           # 市净率（总市值/净资产）
        "ps",                           # 市销率
        "ps_ttm",                       # 市销率（TTM）
        "dv_ratio",                     # 股息率 （%）
        "dv_ttm",                       # 股息率（TTM）（%）
        "total_share",                  # 总股本 （万股）
        "float_share",                  # 流通股本 （万股）
        "free_share",                   # 自由流通股本 （万）
        "total_mv",                     # 总市值 （万元）
        "circ_mv",                      # 流通市值（万元）
        "limit_status"
    ])
    # print(df)
    return df


daily_basic_path = "daily_basic"

# 交易日当天的时间，采用日期格式：yyyyMMdd
def download_trade_date(trade_date):
    if len(trade_date) != 8:
        print("交易日期错误")
    year = trade_date[0:4]

    trade_date_path = daily_basic_path + "/" + trade_date + ".csv"
    if os.path.exists(trade_date_path) is False:
        df = down_load_daily_basic("", trade_date, "", "")
        df.to_csv(trade_date_path)
        print(f"写入成功，{trade_date_path}")
    else:
        print(f"文件存在，{trade_date_path}")

    flat_df = pd.read_csv(trade_date_path)
    print(f"数据总数，{len(flat_df)}")

    merge_num = 0
    for index, flat_row in flat_df.iterrows():
        try:
            ts_code = flat_row["ts_code"]
            ts_code_path = ts_code.replace(".", "_")
            year_file = f"{daily_basic_path}/{ts_code_path}/{year}.csv"
            Path(year_file).parent.mkdir(parents=True, exist_ok=True)

            if os.path.exists(year_file) is False:
                print(f"{year_file}，文件不存在")
                df = down_load_daily_basic(ts_code, "", ZukTuShare.analysis_trade_date_start, trade_date)
                df.to_csv(year_file)
                continue

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

    print(f"合并总数:{merge_num}")
# def download_ts_code(ts_code_path, start_date, end_date):
#     code = ts_code_path.replace("_", ".")
#     path = f"daily_basic/{ts_code_path}/" + "2025.csv"
#     print(path)
#     df = daily_basic(ts_code=code, trade_date="", start_date=start_date, end_date=end_date)
#     df.to_csv(path)





# 采集交易日当天全市场收盘后的信息
if __name__ == '__main__':
    download_trade_date(ZukTuShare.analysis_trade_date)
    print("完成")


