import os
import shutil
import pandas as pd

# 改名
def rename():
    dirs = os.listdir("daily_basic")
    for dir in dirs:
        if (dir == "000000_AA"):
            continue
        files = os.listdir("daily_basic/"+dir)
        for file in files:
            if (file == "2025.csv"):
                continue

            filepath = "daily_basic/"+dir+"/"+file
            exist = os.path.exists(filepath)
            print(f"{filepath}, {exist}")
            new_file = "daily_basic/"+dir+"/2025.csv"
            os.rename(filepath, new_file)


# 合并
def merge():
    print("合并")
    dirs = os.listdir("daily_basic")
    for dir in dirs:
        if(dir=="000000_AA"):
            continue
        files = os.listdir("daily_basic/"+dir)
        files.reverse()
        for file in files:
            if(file=="202511.csv"):
                continue
            csv_file = "daily_basic/" + dir + "/" + file
            exist = os.path.exists(csv_file)

            csv_file_11 = "daily_basic/" + dir + "/202511.csv"
            exist_11 = os.path.exists(csv_file_11)

            try:
                if exist and exist_11:
                    df = pd.read_csv(csv_file)
                    df_11 = pd.read_csv(csv_file_11)
                    merged_df = pd.concat([df_11, df], ignore_index=True)
                    merged_df.to_csv(csv_file_11, index=False)
                    os.remove(csv_file)
                    print(f"{csv_file}->{csv_file_11}")
                else:
                    print(f"{csv_file}, {exist}")
                    print(f"{csv_file_11}, {exist_11}")
                    print("文件不存在")
            except:
                print()


def compare():
    print("比较")
    error_num = 0
    location_num = 0
    stocks = pd.read_csv("all_stocks.csv")
    for index, row in stocks.iterrows():
        ts_code = row["ts_code"].replace(".", "_")
        name = row["name"]
        daily = "daily/" + ts_code + "/2025.csv"
        daily_basic = "daily_basic/" + ts_code + "/2025.csv"
        if os.path.exists(daily) is False or os.path.exists(daily_basic) is False:
            print(f"路径缺失，{ts_code}，{name}")
        print(ts_code, name)
        try:
            daily_df = pd.read_csv(daily)
            daily_basic_df = pd.read_csv(daily_basic)
            if (len(daily_df) != len(daily_basic_df)):
                print(f"数据量不一致，{ts_code}，{name}，{len(daily_df)}， {len(daily_basic_df)}")
                error_num = error_num + 1
            else:
                print(len(daily_df))
                for i in range(len(daily_df)):
                    ts_code = daily_df.iloc[i]["ts_code"]
                    daily_df_row = daily_df.iloc[i]
                    daily_basic_df_row = daily_basic_df.iloc[i]
                    trade_date = daily_df_row["trade_date"]
                    if ((daily_df_row["trade_date"] != daily_basic_df_row["trade_date"])
                            or (daily_df_row["ts_code"] != daily_basic_df_row["ts_code"] )):
                        print(f"{ts_code},{trade_date},位置错误")
                        location_num = location_num + 1
        except:
            print(f"读取失败，{ts_code}，{name}")

    print(f"error_num:{error_num}, location_num:{location_num}")
        

# 比较daily与daily_basic数据是否一致
if __name__ == '__main__':
    # rename()
    # merge()
    compare()
    print("完成")


