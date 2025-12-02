import os

import tushare as ts
import pandas as pd

if __name__ == '__main__':
    trade_date = "20251202"
    path = "rt_k/rt_k.csv"
    df = pd.read_csv(path)
    print(len(df))
    for index, row in df.iterrows():
        ts_code = row['ts_code']
        # ts_code	name	pre_close	high	open	low	close	vol	amount	num
        print(ts_code)
        ts_code_path = ts_code.replace(".", "_")
        module_path = f"module/{ts_code_path}.csv"
        if not os.path.exists(module_path):
            print(f"{module_path}不存在")
            continue
        module_df = pd.read_csv(module_path)
        if module_df.empty or len(module_df) == 0:
            continue

        module_head_row = module_df.iloc[0]

        module_head_row_trade_date = module_head_row["trade_date"]

        if(module_head_row_trade_date == trade_date or str(module_head_row_trade_date) == trade_date):
            # 记录已存在
            continue

        turnover_rate = row['vol'] / module_head_row["float_share"]
        change = (row['close'] - module_head_row["close"]) / module_head_row["close"]

        print(module_head_row["ts_code"], module_head_row["name"])
        # ts_code	name	trade_date	open	high	low	close	pre_close	change	vol	amount	turnover_rate	float_share	area	industry	market
        new_record = {'ts_code': module_head_row["ts_code"],
                      'name': module_head_row["name"],
                      'trade_date': trade_date,
                      'open': row["open"],
                      'high': row["high"],
                      'low': row["low"],
                      'close': row["close"],
                      'pre_close': module_head_row["close"],
                      'change': round(change, 4),
                      'vol': row["vol"],
                      'amount': row["amount"],
                      'turnover_rate': round(turnover_rate, 4),
                      'float_share': module_head_row["float_share"],
                      'area': module_head_row["area"],
                      'industry': module_head_row["industry"],
                      'market': module_head_row["market"]
                      }

        new_df = pd.DataFrame([new_record])

        merge = pd.concat([new_df, module_df], ignore_index=True)
        merge = merge.loc[:, ~merge.columns.str.contains('^Unnamed')]
        merge.to_csv(module_path, index=False)
        print(f"合并成功{module_path}")
        print()

