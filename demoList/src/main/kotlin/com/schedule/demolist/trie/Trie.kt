package com.schedule.demolist.trie

import kotlinx.serialization.Serializable
import java.util.HashSet

@Serializable
class Trie {
    var head: TrieNode = TrieNode()
    fun addTag(word: String){
        var curPoint = 0
        var curNode = head
        while (curPoint < word.length){
            if (curNode.links.containsKey(word[curPoint])) {
                curNode = curNode.links[word[curPoint]]!!
                curPoint += 1
            }else{
                var tmp= TrieNode(word[curPoint])
                curNode.links[word[curPoint]] = tmp
                curPoint += 1
                curNode = tmp
            }
        }
        curNode.endHuh = true
    }
    fun removeTag(word: String){
        if (word.isEmpty()){
            return
        }
        var curNode = head
        var curPoint = 0
        var pastAncestor = head
        var pastIndex = 0
        while (curPoint < word.length){
            if (curNode.links.containsKey(word[curPoint])){
                if (curNode.links.size > 1){
                    pastAncestor = curNode
                    pastIndex = curPoint
                }
                curNode = curNode.links[word[curPoint]]!!
                curPoint += 1
            } else{return}
        }
        if (curNode.links.isNotEmpty()){curNode.endHuh = false}
        else{
            pastAncestor.links.remove(word[pastIndex])
        }
    }
    fun getCandidates(prefix: String): ArrayList<String>{
        var curPoint = 0
        var curNode = head
        while (curPoint < prefix.length){
            if (curNode.links.containsKey(prefix[curPoint])){
                curNode = curNode.links[prefix[curPoint]]!!
                curPoint += 1
            } else{return ArrayList<String>()}
        }
        var cands = curNode.getCandidates()
        for (i in 0 until cands.size){
            cands[i] = prefix + cands[i]
        }
        return cands
    }

    fun populateFromHashSet(arStr: HashSet<String>) {
        arStr.forEach{ addTag(it) }
    }
}

@Serializable
class TrieNode{
    var links = mutableMapOf<Char, TrieNode>()
    var char = 'a'
    var endHuh = false
    constructor(let: Char){
        char = let
    }
    constructor(){
    }
    fun getCandidates(): ArrayList<String>{
        var ans = ArrayList<String>()
        if (endHuh){ans.add("")}
        for (entry in links.keys.iterator()){
            var node = links[entry]!!
            var part = node.getCandidates()
            for (p in part){
                ans.add(entry+p)
            }
        }
        return ans
    }
}