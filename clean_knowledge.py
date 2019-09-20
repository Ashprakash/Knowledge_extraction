from allennlp.predictors.predictor import Predictor
from allennlp.predictors import Predictor
from allennlp.models.archival import load_archive
import json
import nltk
import re 

#model = PretrainedModel('./esim-elmo-2018.05.17.tar.gz','textual-entailment')
#predictor = model.predictor()


class PretrainedModel:
    """
    A pretrained model is determined by both an archive file
    (representing the trained model)
    and a choice of predictor.
    """
    def __init__(self, archive_file: str, predictor_name: str) -> None:
        self.archive_file = archive_file
        self.predictor_name = predictor_name

    def predictor(self) -> Predictor:
        archive = load_archive(self.archive_file)
        return Predictor.from_archive(archive, self.predictor_name)


def rank_knowledge():
    model = PretrainedModel('../esim-elmo-2018.05.17.tar.gz','textual-entailment')
    predictor = model.predictor()
    
    problems = "../data_sets/winogrande/knowledge_queries.json"
    f = open(problems, "r")
    all_probs = f.read()
    knowledge = json.loads(all_probs)
    for i in range(290, len(knowledge)):
        each = knowledge[i][0]
        q = each['question']
        obj = []
        for k in each['knowledge']:
            if len(k)== 0:
                continue
            score = predictor.predict(hypothesis=k,premise=q)["label_probs"]
            o = {}
            o['k'] =  k
            o['score'] = score[0]
            obj.append(o)
        obj.sort(key=lambda x: x['score'], reverse=True)
        each['knowledge'] = obj
        if(len(obj) > 10):
            new_obj = obj[0:10]
        else:
            new_obj = obj
        for e in new_obj:
            out = {}
            out['sentence'] = e['k']
            with open('../data_sets/winogrande/QASRL/winogrande_knowledge_score.txt', 'a') as outfile:
                json.dump(out, outfile)
                outfile.write('\n')
        print(str(i+1)+'/2863')
        
    with open('../data_sets/winogrande/knowledge_queries.json', 'w') as outfile:
        json.dump(knowledge, outfile)

def create_files_for_qasrl():
    problems = "../data_sets/winogrande/knowledge_queries.json"
    f = open(problems, "r")
    all_probs = f.read()
    knowledge = json.loads(all_probs)
    
    file_name = '../data_sets/winogrande/QASRL/winogrande_knowledge_score_323.txt'
    for i in range(323, len(knowledge)):
        each = knowledge[i][0]
        for j in range(0, len(each['knowledge'])):
            if(j == 10):
                break
            k = each['knowledge'][j]
            obj_sent =  {}
            obj_sent['sentence'] = k['k']
            #if(limit == 0):
            #    limit = 2900
            #    file_name = '../data_sets/winogrande/QASRL/winogrande_knowledge_score_'+str(i)+'.txt'
            with open(file_name, 'a') as outfile:
                json.dump(obj_sent, outfile)
                outfile.write('\n')
  
def Find(string): 
    # findall() has been used  
    # with valid conditions for urls in string 
    url = re.findall('http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\), ]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', string) 
    return url

def clean_knowledge_data_set():
    problems = "../data_sets/winogrande/knowledge_queries.json"
    f = open(problems, "r")
    all_probs = f.read()
    knowledge = json.loads(all_probs)
    
    for i in range(0, len(knowledge)):
        each = knowledge[i][0]
        found= False 
        output_k = []
        for j in range(0, len(each['knowledge'])):
            k = each['knowledge'][j]
            urls = Find(k['k'])
            for u in urls:
                k['k'] = k['k'].replace(u, '')
            tokens = nltk.word_tokenize(k['k'].lower())
            text = nltk.Text(tokens)
            tagged = nltk.pos_tag(text)
            found = False
            if(len(tokens) <  7):
                continue
            for word,tag in tagged:
                if('VB' in  tag):
                    found = True
                    break
            if(found):
                output_k.append(k)
        each['knowledge'] = output_k
           
    with open('../data_sets/winogrande/knowledge_queries.json', 'w') as outfile:
       json.dump(knowledge, outfile)
    

if __name__=="__main__":
   create_files_for_qasrl()
            