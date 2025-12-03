import tushare as ts

token = "4dfe55ae66614ca943e09a6d82339eb65b77dcaf327841ba3d5c1574"


def getPro_5000():
    token_5000 = "4dfe55ae66614ca943e09a6d82339eb65b77dcaf327841ba3d5c1574"
    pro = ts.pro_api(token_5000)
    return pro


def getPro_10000():
    pro = ts.pro_api("c1r591l1j03n22x258")
    pro._DataApi__token = 'c1r591l1j03n22x258'
    pro._DataApi__http_url = 'http://proplus.tushare.nlink.vip'
    return pro