import tushare as ts

analysis_trade_date_start = "20260101"
analysis_trade_date = "20260202"

def getPro():
    return getPro_5000()

def getPro_self():
    token_self = "d6a7b03012743e8b035a4c37ec258b77fb5a65500c0629e0022cffde"
    pro = ts.pro_api(token_self)
    return pro

def getPro_5000():
    # 20261124
    token_5000 = "4dfe55ae66614ca943e09a6d82339eb65b77dcaf327841ba3d5c1574"
    pro = ts.pro_api(token_5000)
    return pro


def getPro_10000():
    # 20261201
    token_100000 = "022fe0122b7c9829941beb898d20d5c19db0eb0c62ea8fee51c10000"
    pro = ts.pro_api(token_100000)
    return pro