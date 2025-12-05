public enum ProcessType {
   REAL_TIME(0),
   SYSTEM(1),
   INTERACTIVE(2),
   BATCH(3);

   private final int queueIndex;

   ProcessType(int queueIndex) { 
      this.queueIndex = queueIndex; 
   }

   public int getQueueIndex() { 
      return queueIndex; 
   }

   public static ProcessType from(String s) {
      if (s == null) 
         throw new IllegalArgumentException("null type");
      return switch (s.trim().toUpperCase()) {
         case "REAL-TIME", "REAL_TIME", "REALTIME" -> REAL_TIME;
         case "SYSTEM" -> SYSTEM;
         case "INTERACTIVE" -> INTERACTIVE;
         case "BATCH" -> BATCH;
         default -> throw new IllegalArgumentException("Unknown type: " + s);
      };
   }
}
