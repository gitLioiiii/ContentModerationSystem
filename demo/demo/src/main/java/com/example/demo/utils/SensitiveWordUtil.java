package com.example.demo.utils;

import java.util.*;

// 敏感词过滤工具类
public class SensitiveWordUtil {

    private TrieNode root;
    private int minWordLength = 1;

    // 构造法
    public SensitiveWordUtil(List<String> sensitiveWords) {
        this.root = buildTrie(sensitiveWords);
        this.minWordLength = sensitiveWords.stream()
            .mapToInt(String::length).min().orElse(1);
    }

    // Trie 树
    private TrieNode buildTrie(List<String> words) {
        TrieNode root = new TrieNode();
        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node = node.addChild(c);
            }
            node.setEnd(true);
            node.setKeyword(word);
        }
        return root;
    }

    // 核心 DFA 匹配算法
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.length() < minWordLength) {
            return false;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (dfaMatch(chars, i)) {
                return true;
            }
        }
        return false;
    }

    // DFA 状态转移匹配
    private boolean dfaMatch(char[] chars, int start) {
        TrieNode node = root;

        for (int i = start; i < chars.length; i++) {
            char c = chars[i];

            if (!node.hasChild(c)) {
                break; // 状态转移失败
            }

            node = node.getChild(c);

            if (node.isEnd()) {
                return true; // 到达接受状态
            }
        }
        return false;
    }

    // 查找并替换敏感词
    public String filter(String text, String replacement) {
        List<SensitiveWordResult> words = findAllWords(text);

        // 从后往前替换，避免索引变化问题
        StringBuilder result = new StringBuilder(text);
        for (int i = words.size() - 1; i >= 0; i--) {
            SensitiveWordResult word = words.get(i);
            String stars = String.valueOf(replacement != null ? replacement : "*")
                .repeat(word.getEnd() - word.getStart() + 1);
            result.replace(word.getStart(), word.getEnd() + 1, stars);
        }
        return result.toString();
    }

    // 查找并替换敏感词
    public String filter(String text) {
        return filter(text, "*");
    }

    // 查找所有敏感词
    public List<SensitiveWordResult> findAllWords(String text) {
        List<SensitiveWordResult> results = new ArrayList<>();

        if (text == null || text.length() < minWordLength) {
            return results;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            TrieNode node = root;
            int j = i;

            while (j < chars.length && node.hasChild(chars[j])) {
                node = node.getChild(chars[j]);
                j++;

                if (node.isEnd()) {
                    results.add(new SensitiveWordResult(
                        text.substring(i, j), i, j - 1));
                }
            }
        }
        return results;
    }

    // Trie 节点类
    public static class TrieNode {
        // 子节点映射：字符 -> Trie节点
        private Map<Character, TrieNode> children = new HashMap<>();

        // 是否为敏感词的结束节点
        private boolean isEnd = false;

        // 完整敏感词内容（便于输出）
        private String keyword;

        public TrieNode getChild(char c) {
            return children.get(c);
        }

        public TrieNode addChild(char c) {
            return children.computeIfAbsent(c, k -> new TrieNode());
        }

        public boolean hasChild(char c) {
            return children.containsKey(c);
        }

        public boolean isEnd() {
            return isEnd;
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Map<Character, TrieNode> getChildren() {
            return children;
        }
    }

    // 敏感词结果封装类
    public static class SensitiveWordResult {
        private String word;      // 敏感词内容
        private int start;        // 起始位置
        private int end;          // 结束位置

        public SensitiveWordResult(String word, int start, int end) {
            this.word = word;
            this.start = start;
            this.end = end;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return "SensitiveWordResult{" +
                    "word='" + word + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }
}
