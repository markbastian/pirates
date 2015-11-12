(defn setup-winner-watch [state]
  (add-watch state :watch-for-winner
    (fn [_ _ _ n]
      (if (rules/winner? n)
        (SwingUtilities/invokeLater
          (JOptionPane/showMessageDialog
            nil
            (str (key (rules/active-player n)) " is the winner!")))))))

