
from urllib.parse import urlparse
from lxml.html import fromstring
import lxml
from requests import get
from bs4 import BeautifulSoup
import json

def scrape_google(search_query,s_q_id):
    google_url = "https://google.com/search?q="+search_query
    raw = get(google_url).text
    soup = BeautifulSoup(raw, "lxml")
    x = soup.body.find_all('div', attrs={'class' : 'g'})
    
    # kill all script and style elements
    for script in soup(["script", "style", "meta", "input", "title"]):
        script.extract()    # rip it out  

    # get text
    #text = soup.get_text();
    spans = soup.body.find_all('div')
    
    print(soup)
    for each in spans:
        print(each)
    print(text)
    # break into lines and remove leading and trailing space on each
    #lines = (line.strip() for line in text.splitlines()
    # break multi-headlines into a line each
    #chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
    # drop blank lines
    #text = '\n'.join(chunk for chunk in chunks if chunk)
    
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


def scrape_google_update(search_query, s_q_id):
    google_url = "https://google.com/search?q="+search_query
    raw = get(google_url).text
    soup = BeautifulSoup(raw, "lxml")
    
    # kill all script and style elements
    for script in soup(["script", "style", "meta", "input", "title"]):
       script.extract()    # rip it out  

    # get text
    text = soup.get_text();
    raw_text = text.split(' ...')
    scrapped = []
    for j in range(0, len(raw_text)):
        if(j == 0):
            continue
        if(j == len(raw_text)-1):
            c = raw_text[j].split('Related')
            scrapped.append(c[0])
            continue
        scrapped.append(raw_text[j])
        print("********************")
        print(raw_text[j])
        print("********************")
    return scrapped     

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
        print(i)
        scrape_result = scrape_google(query,i)
        result[query] = scrape_result
        i = i + 1

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
    problems = "../data_sets/winogrande/knowledge_queries.json"
    f = open(problems,"r")
    all_probs = f.read()
    grande_problems = json.loads(all_probs)
    
    for i in range(50, len(grande_problems)):
        prob = grande_problems[i][0]
        query = prob['queries'][0]
        search_result = scrape_google_update(query, 0)
        prob["knowledge"] = search_result
        with open('../data_sets/winogrande/knowledge_queries.json', 'w') as outfile:
            json.dump(grande_problems, outfile)
    
 