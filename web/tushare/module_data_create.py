import os

import tushare as ts
import pandas as pd
import ZukTuShare

pro = ts.pro_api(ZukTuShare.token)

daily_path = "daily"
daily_basic_path = "daily_basic"

module_path = "module"

max = 120

def create_module_date():
    print("创建分析数据")
    all_stocks = pd.read_csv("all_stocks.csv", encoding="utf-8")
    for index, stock_row in all_stocks.iterrows():
        ts_code = stock_row["ts_code"]
        name = stock_row["name"]
        area = stock_row["area"]
        industry = stock_row["industry"]
        market = stock_row["market"]

        print(ts_code, name)
        ts_code_path = ts_code.replace(".", "_")

        # 构建数据文件的路径
        daily_file = daily_path + "/" + ts_code_path + "/" + "2025.csv"
        daily_basic_file = daily_basic_path + "/" + ts_code_path + "/" + "2025.csv"

        if not os.path.exists(daily_file) or not os.path.exists(daily_basic_file):
            # 判断数据文件路径是否存在
            print(f"{daily_file}, {daily_basic_file} 文件不存在")
            continue

        daily_df = pd.read_csv(daily_file, encoding="utf-8")
        daily_basic_df = pd.read_csv(daily_basic_file, encoding="utf-8")

        if len(daily_df) < max or len(daily_basic_df) < max:
            print(f"{ts_code},{name},可能是新股，数量不足30")
            continue

        daily_df_top_30 = daily_df[:max]
        daily_basic_df_top_30 = daily_basic_df[:max]

        # 判断数据是否一致
        compare_ok = True
        for i in range(len(daily_basic_df_top_30)):
            if(compare_ok):
                if ((daily_df_top_30.iloc[i]["trade_date"] != daily_basic_df_top_30.iloc[i]["trade_date"])
                    or (daily_df_top_30.iloc[i]["ts_code"] != daily_basic_df_top_30.iloc[i]["ts_code"])):
                    compare_ok = False
        if(compare_ok is False):
            print("数据不一致")
            continue

        sub_df = pd.DataFrame({
            "ts_code": daily_df_top_30["ts_code"],                      # 股票代码
            "name": name,                                               # 股票名称
            "trade_date": daily_df_top_30["trade_date"],                # 交易日
            "open": daily_df_top_30["open"],                            # 开盘价
            "high": daily_df_top_30["high"],                            # 最高价
            "low": daily_df_top_30["low"],                              # 最低价
            "close": daily_df_top_30["close"],                          # 收盘价
            "pre_close": daily_df_top_30["pre_close"],                  # 上一个交易日收盘价
            "change": daily_df_top_30["pct_chg"],                       # 涨跌幅度（区别涨跌额）
            "vol": daily_df_top_30["vol"],                              # 成交量
            "amount": daily_df_top_30["amount"],                        # 成交额
            "turnover_rate": daily_basic_df_top_30["turnover_rate"],    # 换手率
            "float_share": daily_basic_df_top_30["float_share"],        # 流通数
            "area": area,                                               # 所在地区
            "industry": industry,                                       # 行业
            "market": market,                                           # 市场类型：主板、创业、科创、北交
        })

        module_file = f"{module_path}/{ts_code.replace('.', '_')}.csv"
        try:
            if not os.path.exists(module_file):
                sub_df.to_csv(module_file, encoding='utf-8', index=False)
                print("写入成功")

            else:
                print("已经存在")
        except Exception as e:
            print(e)


if __name__ == '__main__':
    # 创建分析数据
    create_module_date()