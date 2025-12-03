import os

import tushare as ts
import pandas as pd

if __name__ == '__main__':
    trade_date = "20251203"
    path = "rt_k/rt_k.csv"
    df = pd.read_csv(path, encoding="utf-8")
    print(len(df))
    for index, rtk_row in df.iterrows():
        ts_code = rtk_row['ts_code']
        # ts_code	name	pre_close	high	open	low	close	vol	amount	num
        print(ts_code, rtk_row['name'])
        ts_code_path = ts_code.replace(".", "_")
        module_path = f"module/{ts_code_path}.csv"
        if not os.path.exists(module_path):
            print(f"{module_path}不存在")
            continue
        module_df = pd.read_csv(module_path, encoding="utf-8")
        if module_df.empty or len(module_df) == 0:
            continue

        # 取第一条记录
        module_first_row = module_df.iloc[0]
        if(module_first_row["trade_date"] == trade_date or str(module_first_row["trade_date"]) == trade_date):
            # 记录已存在
            continue

        rtk_vol = rtk_row['vol'] / 100  #交易量
        rtk_amount = rtk_row['amount'] / 1000 #交易额
        # 计算换手率
        turnover_rate = rtk_vol / module_first_row["float_share"]
        # 计算涨跌
        change = (rtk_row['close'] - module_first_row["close"]) / module_first_row["close"]
        change = change * 100
        print(module_first_row["ts_code"], module_first_row["name"])
        # ts_code	name	trade_date	open	high	low	close	pre_close	change	vol	amount	turnover_rate	float_share	area	industry	market
        new_record = {'ts_code': module_first_row["ts_code"],
                      'name': module_first_row["name"],
                      'trade_date': trade_date,
                      'open': rtk_row["open"],
                      'high': rtk_row["high"],
                      'low': rtk_row["low"],
                      'close': rtk_row["close"],
                      'pre_close': module_first_row["close"],
                      'change': round(change, 4),
                      'vol': rtk_vol,
                      'amount': rtk_amount,
                      'turnover_rate': round(turnover_rate, 4),
                      'float_share': module_first_row["float_share"],
                      'area': module_first_row["area"],
                      'industry': module_first_row["industry"],
                      'market': module_first_row["market"]
                      }

        new_df = pd.DataFrame([new_record])

        merge = pd.concat([new_df, module_df], ignore_index=True)
        merge = merge.loc[:, ~merge.columns.str.contains('^Unnamed')]
        merge.to_csv(module_path, encoding="utf-8", index=False)
        print(f"合并成功{module_path}")
        print()

