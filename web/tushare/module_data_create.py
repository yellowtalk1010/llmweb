import os

import tushare as ts
import pandas as pd
import ZukTuShare


daily_path = "daily"
daily_basic_path = "daily_basic"

module_path = "module"

max = 120 # 提取最大数量 120 条记录

# 判断是否「严格递减」
def is_decreasing(lst):
    return all(lst[i] > lst[i+1] for i in range(len(lst) - 1))


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
        #
        daily_file_2026 = daily_path + "/" + ts_code_path + "/" + "2026.csv"
        daily_basic_file_2026 = daily_basic_path + "/" + ts_code_path + "/" + "2026.csv"
        if not os.path.exists(daily_file_2026) or not os.path.exists(daily_basic_file_2026):
            # 判断数据文件路径是否存在
            print(f"{daily_file_2026}, {daily_basic_file_2026} 文件不存在")
            continue
        daily_df_2026 = pd.read_csv(daily_file_2026, encoding="utf-8")
        daily_basic_df_2026 = pd.read_csv(daily_basic_file_2026, encoding="utf-8")

        ##
        daily_file_2025 = daily_path + "/" + ts_code_path + "/" + "2025.csv"
        daily_basic_file_2025 = daily_basic_path + "/" + ts_code_path + "/" + "2025.csv"
        if not os.path.exists(daily_file_2025) or not os.path.exists(daily_basic_file_2025):
            # 判断数据文件路径是否存在
            print(f"{daily_file_2025}, {daily_basic_file_2025} 文件不存在")
            continue
        daily_df_2025 = pd.read_csv(daily_file_2025, encoding="utf-8")
        daily_basic_df_2025 = pd.read_csv(daily_basic_file_2025, encoding="utf-8")

        # 上下拼接
        daily_df = pd.concat([daily_df_2026, daily_df_2025], ignore_index=True)
        daily_basic_df = pd.concat([daily_basic_df_2026, daily_basic_df_2025], ignore_index=True)

        if(len(daily_df) != len(daily_basic_df)):
            print(f"{len(daily_df)}, {len(daily_basic_df)}数据不相等，严重异常")
            continue

        tradedate_list = list(daily_df["trade_date"])
        if not is_decreasing(tradedate_list):
            print("交易日期异常，严重错误")

        if len(daily_df) < max or len(daily_basic_df) < max:
            print(f"{ts_code},{name},可能是新股，数量不足{max}")
            continue

        daily_df_top_max = daily_df[:max]
        daily_basic_df_top_max = daily_basic_df[:max]

        # 判断数据是否一致
        compare_ok = True
        for i in range(len(daily_basic_df_top_max)):
            if(compare_ok):
                if ((daily_df_top_max.iloc[i]["trade_date"] != daily_basic_df_top_max.iloc[i]["trade_date"])
                    or (daily_df_top_max.iloc[i]["ts_code"] != daily_basic_df_top_max.iloc[i]["ts_code"])):
                    compare_ok = False
        if(compare_ok is False):
            print("数据不一致")
            continue

        sub_df = pd.DataFrame({
            "ts_code": daily_df_top_max["ts_code"],                      # 股票代码
            "name": name,                                               # 股票名称
            "trade_date": daily_df_top_max["trade_date"],                # 交易日
            "open": daily_df_top_max["open"],                            # 开盘价
            "high": daily_df_top_max["high"],                            # 最高价
            "low": daily_df_top_max["low"],                              # 最低价
            "close": daily_df_top_max["close"],                          # 收盘价
            "pre_close": daily_df_top_max["pre_close"],                  # 上一个交易日收盘价
            "change": daily_df_top_max["pct_chg"],                       # 涨跌幅度（区别涨跌额）
            "vol": daily_df_top_max["vol"],                              # 成交量
            "amount": daily_df_top_max["amount"],                        # 成交额
            "turnover_rate": daily_basic_df_top_max["turnover_rate"],    # 换手率
            "float_share": daily_basic_df_top_max["float_share"],        # 流通数
            "area": area,                                               # 所在地区
            "industry": industry,                                       # 行业
            "market": market,                                           # 市场类型：主板、创业、科创、北交
            "total_mv": daily_basic_df_top_max["total_mv"],              # 总市值
            "limit": "N"                                                 # U涨停，D跌停，Z炸版，N未处理
        })

        if(not os.path.exists(module_path)):
            # 创建module文件夹
            os.makedirs(module_path)
        module_file = f"{module_path}/{ts_code.replace('.', '_')}.csv"

        try:
            if not os.path.exists(module_file):
                sub_df.to_csv(module_file, encoding='utf-8', index=False)
                print("写入成功")

            else:
                print("已经存在")
        except Exception as e:
            print(e)

# 写入炸板数据
def limit():
    limit_path = "limit_list_d/data"
    if os.path.exists(limit_path):
        files = os.listdir(limit_path)
        files.sort(reverse=True)  # 使用 reverse=True 进行降序排序
        print(len(files))
        df_list = []
        for file_name in files[:max]: # 取前120条
            file_path = os.path.join(limit_path, file_name)
            # print(f"{file_path}, {os.path.exists(file_path)}")
            df = pd.read_csv(file_path, encoding="utf-8")
            df_selected = df[['ts_code',
                              'trade_date',
                              'name',
                              'open_times',     # 炸板次数
                              'up_stat',        # 涨停统计
                              'limit_times',    # 连板数
                              'limit'           # D跌停U涨停Z炸板
                              ]].copy()

            df_list.append(df_selected)

        if df_list:
            combined_df = pd.concat(df_list, ignore_index=True)
            # print(f"合并完成，总行数: {len(combined_df)}")
            sorted_df = combined_df.sort_values(
                by=['ts_code', 'trade_date'],
                ascending=[True, False]
            ).reset_index(drop=True)
            print(f"排序完成，总行数: {len(sorted_df)}")
            for index, row in sorted_df.iterrows():
                ts_code = row['ts_code']
                name = row['name']
                trade_date = row['trade_date']
                limit = row['limit']
                # print(f"{ts_code}, {name}, {trade_date}, {limit}")
                ts_code_path = ts_code.replace(".", "_")
                module_file = f"{module_path}/{ts_code_path}.csv"
                if os.path.exists(module_file):
                    module_file_df = pd.read_csv(module_file, encoding="utf-8")
                    # 查找 trade_date 等于当前日期的行
                    mask = module_file_df['trade_date'] == trade_date
                    if mask.sum() > 0:
                        module_file_df.loc[mask, 'limit'] = limit
                        module_file_df.to_csv(module_file, encoding='utf-8', index=False)
                        print(f"{ts_code}, {name}, {trade_date}, {limit}")

if __name__ == '__main__':
    # 创建分析数据
    create_module_date()
    # 写入炸板数据
    limit()