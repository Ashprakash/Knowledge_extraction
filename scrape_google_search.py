
from urlparse import urlparse
from lxml.html import fromstring
import lxml
from requests import get
from bs4 import BeautifulSoup
import urllib2
import json
import pickle

def scrape_google(search_query,s_q_id):
    google_url = "https://google.com/search?q="+search_query
    raw = get(google_url).text
    soup = BeautifulSoup(raw,"lxml")
    x = soup.body.find_all('div', attrs={'class' : 'g'})
    
    result = []    

    for item in x:
        url_ = None
        txst = None

        ul = item.find('h3', attrs={'class' : 'r'})
        txt = item.find('span', attrs={'class' : 'st'})
        if ul is not None:
            #print "R"
            url_ = ul.find('a')
            if url_ is not None:
                url_ = url_.attrs['href']
            if url_ is not None:
                if url_.startswith("/url?q="):
                    url_ = url_[7:]
            #print url_
        
        if txt is not None:
            #print "S"
            txst = txt.text
            #print txt

        #print "********************"
        if url_ is not None or txst is not None:
            url_text = {}
            url_text["url"] = url_
            url_text["text"] = txst
            result.append(url_text)
    return result            


def search_and_save(queries_file):
    f_json = open(queries_file,'r')
    queries_arr = json.load(f_json)

    set_of_queries = set()

    q_num = 0
    for item in queries_arr:
        qs = item["queries"]
        for q in qs:
            set_of_queries.add(q)
        q_num += len(qs)

    #print q_num
    #print len(set_of_queries)
    i=1
    result = {}
    for query in set_of_queries:
        print i
        scrape_result = scrape_google(query,i)
        result[query] = scrape_result
        i+=1

    return result

def search_copa_sents(queries_file):
    f_json = open(queries_file,'r')
    probs_arr = json.load(f_json)

    set_of_queries = set()

    result = []

    q_num = 0
    for prob in probs_arr:
        new_prob = prob
        alt1_queries = prob["queries_alt1"]
        alt1_k_sents = []
        for alt1_query in alt1_queries:
            scrape_result = scrape_google(alt1_query,0)
            alt1_k_sents.extend(scrape_result)
        
        alt2_queries = prob["queries_alt2"]
        alt2_k_sents = []
        for alt2_query in alt2_queries:
            scrape_result = scrape_google(alt2_query,0)
            alt2_k_sents.extend(scrape_result)
        
        new_prob["alt1_k_sents"] = alt1_k_sents
        new_prob["alt2_k_sents"] = alt2_k_sents

    return result

if __name__=="__main__":
    
    search_query = "\" * eat * because * tasty * \""
    search_result = scrape_google(search_query,0)
    print "********************"
    print "********************"

    





 
